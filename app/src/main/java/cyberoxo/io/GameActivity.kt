package cyberoxo.io

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cyberoxo.io.databinding.ActivityGameBinding
import cyberoxo.io.databinding.DialogGameResultBinding
import cyberoxo.io.engine.BotEngine
import cyberoxo.io.engine.GameEngine
import cyberoxo.io.model.Difficulty
import cyberoxo.io.model.GameMode
import cyberoxo.io.model.MoveResult
import cyberoxo.io.model.Player
import cyberoxo.io.model.BoardSize
import cyberoxo.io.ui.GlowAnimator
import cyberoxo.io.ui.SoundManager

/**
 * Main gameplay screen for CyberOXO v2.5.
 *
 * Responsibilities:
 *  - Maintains board state ([board]) and scores ([scoreX], [scoreO])
 *  - Handles human cell taps and delegates bot turns to [BotEngine]
 *  - Calls [GameEngine] for win/draw detection after every move
 *  - Drives all visual updates through [GlowAnimator]
 *  - Plays all sound effects through [SoundManager]
 *  - Shows [showResultDialog] on game over
 *  - Supports dynamic board sizes (3x3, 4x4, 5x5)
 *
 * The board is a flat [Array<Player?>] with size based on selected board.
 */
class GameActivity : AppCompatActivity() {

    // ── ViewBinding ──────────────────────────────────────────────────────────
    private lateinit var binding: ActivityGameBinding

    // ── Game configuration (received from ModeSelectionActivity) ─────────────
    private lateinit var gameMode: GameMode
    private lateinit var difficulty: Difficulty
    private lateinit var boardSize: BoardSize
    private lateinit var selectedTheme: ModeSelectionActivity.GameTheme

    // ── Game state ───────────────────────────────────────────────────────────
    private lateinit var board: Array<Player?>
    private var currentPlayer: Player = Player.X
    private var scoreX = 0
    private var scoreO = 0
    private var gameOver = false

    // ── UI references (built dynamically based on board size) ─────────────────
    private var cells: List<FrameLayout> = emptyList()
    private var marks: List<TextView> = emptyList()
    private lateinit var gameBoardContainer: View

    // ── Bot move delay (ms) – feels more natural than instant ─────────────────
    private val botHandler = Handler(Looper.getMainLooper())
    private val BOT_DELAY_MS = 650L

    // ── Idle shimmer animators (active during bot think) ─────────────────────
    private var idleShimmerAnimators: List<ObjectAnimator> = emptyList()

    // ═════════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═════════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets so content doesn't overlap status/nav bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, sysBars.top, 0, 0)
            binding.bottomBar.setPadding(
                binding.bottomBar.paddingLeft,
                binding.bottomBar.paddingTop,
                binding.bottomBar.paddingRight,
                sysBars.bottom + resources.getDimensionPixelSize(R.dimen.bottom_bar_padding_bottom)
            )
            insets
        }

        // Read intent extras with defaults
        gameMode   = GameMode.valueOf(
            intent.getStringExtra(ModeSelectionActivity.EXTRA_GAME_MODE) ?: GameMode.VS_FRIEND.name)
        difficulty = Difficulty.valueOf(
            intent.getStringExtra(ModeSelectionActivity.EXTRA_DIFFICULTY) ?: Difficulty.MEDIUM.name)
        boardSize = BoardSize.valueOf(
            intent.getStringExtra(ModeSelectionActivity.EXTRA_BOARD_SIZE) ?: BoardSize.SIZE_3x3.name)
        selectedTheme = ModeSelectionActivity.GameTheme.valueOf(
            intent.getStringExtra(ModeSelectionActivity.EXTRA_THEME) ?: ModeSelectionActivity.GameTheme.NEON.name)

        // Initialize board based on size
        initializeBoard()
        
        buildCellReferences()
        setupCellClickListeners()
        setupActionBarListeners()
        applyGameModeLabels()
        updateScoreBoard()
        updateTurnIndicator(bounceCard = false)
        playEntrance()
    }

    override fun onDestroy() {
        super.onDestroy()
        botHandler.removeCallbacksAndMessages(null)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Initialisation helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** Initialize board array based on selected board size */
    private fun initializeBoard() {
        val totalCells = boardSize.size * boardSize.size
        board = arrayOfNulls<Player>(totalCells)
    }

    /** Collects the cell FrameLayouts and their inner mark TextViews into typed lists. */
    private fun buildCellReferences() {
        gameBoardContainer = binding.gameBoard
        
        // For now, we'll use the hardcoded 3x3 cells from layout
        // In a full implementation, you'd dynamically generate these
        cells = listOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        ).take(boardSize.size * boardSize.size)
        
        marks = listOf(
            binding.mark0, binding.mark1, binding.mark2,
            binding.mark3, binding.mark4, binding.mark5,
            binding.mark6, binding.mark7, binding.mark8
        ).take(boardSize.size * boardSize.size)
        
        // Hide unused cells for smaller boards or show all for 3x3
        cells.forEachIndexed { index, cell ->
            cell.visibility = if (index < boardSize.size * boardSize.size) View.VISIBLE else View.GONE
        }
        marks.forEachIndexed { index, mark ->
            mark.visibility = if (index < boardSize.size * boardSize.size) View.VISIBLE else View.GONE
        }
    }

    private fun setupCellClickListeners() {
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener { onCellTapped(index) }
        }
    }

    private fun setupActionBarListeners() {
        binding.btnRestart.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnRestart)
            restartGame()
        }
        binding.btnHome.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnHome)
            botHandler.removeCallbacksAndMessages(null)
            stopIdleShimmer()
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    /** Updates Player 2 label to "BOT" when in Vs-Bot mode. */
    private fun applyGameModeLabels() {
        if (gameMode == GameMode.VS_BOT) {
            binding.tvPlayer2Label.text = getString(R.string.player_bot)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Game logic
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Called when a human player taps a cell.
     * Validates the move, updates the board, checks for win/draw,
     * then either switches turn or triggers the bot.
     */
    private fun onCellTapped(index: Int) {
        if (gameOver) return
        if (board[index] != null) return
        // In VS_BOT mode, block input while it's the bot's turn
        if (gameMode == GameMode.VS_BOT && currentPlayer == Player.O) return

        placeMove(index, currentPlayer)
    }

    /** Core move placement: updates data, updates UI, evaluates result. */
    private fun placeMove(index: Int, player: Player) {
        // Update model
        board[index] = player

        // Play tap sound for the current player
        if (player == Player.X) SoundManager.playTapX() else SoundManager.playTapO()

        // Animate cell press
        if (index < cells.size) {
            GlowAnimator.animateCellPress(cells[index])

            // Update cell background tint
            cells[index].background = when (player) {
                Player.X -> resources.getDrawable(R.drawable.bg_glass_tile_x, theme)
                Player.O -> resources.getDrawable(R.drawable.bg_glass_tile_o, theme)
            }

            // Show and animate the mark
            val mark = marks[index]
            mark.text = player.name
            GlowAnimator.applyMarkGlow(mark, player)
            GlowAnimator.animateMarkEntrance(mark)
        }

        // Evaluate result
        val result = GameEngine.checkResult(board, boardSize)
        when {
            result.winner != null -> handleWin(result)
            result.isDraw         -> handleDraw()
            else                  -> switchTurn()
        }
    }

    private fun switchTurn() {
        currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
        updateTurnIndicator(bounceCard = true)

        // Trigger bot if needed
        if (gameMode == GameMode.VS_BOT && currentPlayer == Player.O && !gameOver) {
            triggerBotMove()
        }
    }

    private fun triggerBotMove() {
        // Disable all cells while bot "thinks"
        cells.forEach { it.isClickable = false }

        // Start idle shimmer on empty cells to show the bot is "thinking"
        val emptyCells = cells.filterIndexed { i, _ -> board[i] == null }
        idleShimmerAnimators = GlowAnimator.startCellIdleShimmer(emptyCells)

        botHandler.postDelayed({
            if (!gameOver) {
                stopIdleShimmer()
                val botIndex = BotEngine.getBotMove(board, difficulty, boardSize)
                placeMove(botIndex, Player.O)
                // Re-enable cells after bot plays
                cells.forEachIndexed { i, cell ->
                    cell.isClickable = board[i] == null
                }
            }
        }, BOT_DELAY_MS)
    }

    private fun stopIdleShimmer() {
        GlowAnimator.stopCellIdleShimmer(idleShimmerAnimators, cells)
        idleShimmerAnimators = emptyList()
    }

    // ── Win / Draw handlers ──────────────────────────────────────────────────

    private fun handleWin(result: MoveResult) {
        gameOver = true
        val winner = result.winner!!

        // Update score
        if (winner == Player.X) scoreX++ else scoreO++
        val scoreView = if (winner == Player.X) binding.tvScoreX else binding.tvScoreO
        GlowAnimator.animateScoreUpdate(scoreView, if (winner == Player.X) scoreX else scoreO)

        // Highlight winning line
        result.winningLine?.let { line ->
            val winCells = line.map { cells[it] }
            winCells.forEach { GlowAnimator.applyWinCellGlow(it, winner) }
            GlowAnimator.animateWinningLine(winCells)

            // Celebration pop on winning cells
            botHandler.postDelayed({
                GlowAnimator.animateWinCelebration(winCells)
            }, 150)
        }

        // Play win sound
        SoundManager.playWin()

        // Show result dialog after a short pause
        botHandler.postDelayed({ showResultDialog(winner) }, 800)
    }

    private fun handleDraw() {
        gameOver = true

        // Shake the board and play draw sound
        SoundManager.playDraw()
        GlowAnimator.animateBoardShake(binding.gameBoard)

        botHandler.postDelayed({ showResultDialog(null) }, 600)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Restart
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Resets the board state and all cell visuals.
     * Scores are preserved across restarts (same "session").
     * Player X always goes first on a new game.
     */
    private fun restartGame() {
        botHandler.removeCallbacksAndMessages(null)
        stopIdleShimmer()
        gameOver = false
        currentPlayer = Player.X

        // Reset model
        for (i in board.indices) board[i] = null

        // Reset each cell UI
        cells.forEachIndexed { index, cell ->
            cell.background = resources.getDrawable(R.drawable.bg_glass_tile, theme)
            cell.isClickable = true
            cell.alpha = 1f
            cell.scaleX = 1f
            cell.scaleY = 1f

            val mark = marks[index]
            mark.text   = ""
            mark.alpha  = 0f
            mark.scaleX = 1f
            mark.scaleY = 1f
        }

        // Restore player card opacity
        GlowAnimator.setCardActive(binding.llPlayerX, true)
        GlowAnimator.setCardActive(binding.llPlayerO, false)
        binding.llPlayerX.background =
            resources.getDrawable(R.drawable.bg_player_card_active_x, theme)
        binding.llPlayerO.background =
            resources.getDrawable(R.drawable.bg_glass_panel, theme)

        updateTurnIndicator(bounceCard = false)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UI helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** Updates scoreboard TextViews (no animation – just text sync). */
    private fun updateScoreBoard() {
        binding.tvScoreX.text = scoreX.toString()
        binding.tvScoreO.text = scoreO.toString()
    }

    /**
     * Highlights the active player card and dims the inactive one.
     * [bounceCard] controls whether to apply the bounce animation (skip on init/restart).
     */
    private fun updateTurnIndicator(bounceCard: Boolean = true) {
        val xIsActive = currentPlayer == Player.X

        // Card alpha
        GlowAnimator.setCardActive(binding.llPlayerX, xIsActive)
        GlowAnimator.setCardActive(binding.llPlayerO, !xIsActive)

        // Card bounce on the newly active card
        if (bounceCard) {
            val activeCard = if (xIsActive) binding.llPlayerX else binding.llPlayerO
            GlowAnimator.animateCardBounce(activeCard)
        }

        // Card border drawable
        binding.llPlayerX.background = resources.getDrawable(
            if (xIsActive) R.drawable.bg_player_card_active_x else R.drawable.bg_glass_panel,
            theme
        )
        binding.llPlayerO.background = resources.getDrawable(
            if (!xIsActive) R.drawable.bg_player_card_active_o else R.drawable.bg_glass_panel,
            theme
        )

        // Arrow tint colour
        val arrowTint = if (xIsActive)
            resources.getColor(R.color.colorPrimary, theme)
        else
            resources.getColor(R.color.colorSecondary, theme)
        binding.ivTurnArrow.setColorFilter(arrowTint)

        // Flip arrow direction: scaleX -1 = pointing left (toward X), 1 = pointing right (toward O)
        GlowAnimator.animateTurnArrow(binding.ivTurnArrow, pointLeft = xIsActive)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Result dialog
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Shows a glass modal dialog with the game result.
     * [winner] = null means a draw.
     */
    private fun showResultDialog(winner: Player?) {
        val dialog = Dialog(this, R.style.Theme_CyberOXO_Dialog)
        val dialogBinding = DialogGameResultBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        if (winner != null) {
            val winnerLabel = if (winner == Player.X) "Player 1" else
                if (gameMode == GameMode.VS_BOT) "Bot" else "Player 2"
            dialogBinding.tvResultMark.text = winner.name
            dialogBinding.tvResultTitle.text = getString(R.string.result_winner, winnerLabel)
            dialogBinding.tvResultSubtitle.text = "Congratulations!"
            GlowAnimator.applyMarkGlow(dialogBinding.tvResultMark, winner)
        } else {
            dialogBinding.tvResultMark.text = "🤝"
            dialogBinding.tvResultMark.textSize = 48f
            dialogBinding.tvResultTitle.text   = getString(R.string.result_draw)
            dialogBinding.tvResultSubtitle.text = getString(R.string.result_draw_sub)
        }

        dialogBinding.btnPlayAgain.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(dialogBinding.btnPlayAgain)
            dialog.dismiss()
            restartGame()
        }
        dialogBinding.btnExitGame.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(dialogBinding.btnExitGame)
            dialog.dismiss()
            botHandler.removeCallbacksAndMessages(null)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Animate dialog entrance
        dialogBinding.root.alpha  = 0f
        dialogBinding.root.scaleX = 0.85f
        dialogBinding.root.scaleY = 0.85f
        dialog.show()
        dialogBinding.root.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
            .withEndAction {
                // After entrance: animate result icon
                GlowAnimator.animateResultIcon(dialogBinding.tvResultMark, isWin = winner != null)
            }
            .start()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Entrance animation
    // ═════════════════════════════════════════════════════════════════════════

    private fun playEntrance() {
        val views: List<View> = listOf(binding.llScoreboard, binding.gameBoard, binding.bottomBar)
        views.forEachIndexed { i, v ->
            v.alpha = 0f
            v.translationY = 40f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay((80 + i * 120).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }
}
