package cyberoxo.io

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cyberoxo.io.databinding.ActivityModeSelectionBinding
import cyberoxo.io.model.Difficulty
import cyberoxo.io.model.GameMode
import cyberoxo.io.model.BoardSize
import cyberoxo.io.ui.GlowAnimator
import cyberoxo.io.ui.SoundManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable

/**
 * MODE SELECTION DASHBOARD – "Cyber Nexus" Redesign
 * ═══════════════════════════════════════════════════
 * 
 * A futuristic dashboard featuring:
 * • Theme selection (Neon, Matrix, Sunset, Ice, Gold)
 * • Board size selector (3x3, 4x4, 5x5)
 * • Difficulty picker with visual indicators
 * • Player stats display
 * • Animated holographic cards
 * • Glassmorphism effects
 * • Dynamic color schemes per theme
 */
class ModeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeSelectionBinding
    private lateinit var prefs: SharedPreferences
    
    // Keep reference so we can cancel in onDestroy
    private var logoFloatAnimator: ObjectAnimator? = null
    
    // Current selected options
    private var selectedTheme: GameTheme = GameTheme.NEON
    private var selectedBoardSize: BoardSize = BoardSize.SIZE_3x3
    private var selectedDifficulty: Difficulty = Difficulty.MEDIUM
    
    companion object {
        const val EXTRA_GAME_MODE      = "EXTRA_GAME_MODE"
        const val EXTRA_DIFFICULTY     = "EXTRA_DIFFICULTY"
        const val EXTRA_BOARD_SIZE     = "EXTRA_BOARD_SIZE"
        const val EXTRA_THEME          = "EXTRA_THEME"
        const val PREFS_NAME           = "cyberoxo_prefs"
        const val KEY_THEME            = "selected_theme"
        const val KEY_BOARD_SIZE       = "selected_board_size"
    }
    
    /** Available themes with unique color schemes */
    enum class GameTheme(
        val displayName: String,
        val primaryColor: String,
        val secondaryColor: String,
        val accentColor: String,
        val backgroundGradient: String,
        val icon: String
    ) {
        NEON("Neon Cyber", "#FF2D55", "#00F5FF", "#7B2FFF", "neon_gradient", "🌟"),
        MATRIX("Matrix Code", "#00FF41", "#008F11", "#003D00", "matrix_gradient", "💚"),
        SUNSET("Sunset Wave", "#FF6B35", "#F7C59F", "#2EC4B6", "sunset_gradient", "🌅"),
        ICE("Ice Storm", "#00D4FF", "#7B2FFF", "#FFFFFF", "ice_gradient", "❄️"),
        GOLD("Golden Elite", "#FFD700", "#FFA500", "#FF8C00", "gold_gradient", "👑")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize preferences
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSavedSettings()

        // Apply window insets so content clears status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, sysBars.top, 0, sysBars.bottom)
            insets
        }

        setupDashboardUI()
        setupCardAnimations()
        setupClickListeners()
        playEntrance()
        applyTheme(selectedTheme)
    }
    
    /** Load saved settings from SharedPreferences */
    private fun loadSavedSettings() {
        val themeName = prefs.getString(KEY_THEME, GameTheme.NEON.name) ?: GameTheme.NEON.name
        val boardSizeName = prefs.getString(KEY_BOARD_SIZE, BoardSize.SIZE_3x3.name) ?: BoardSize.SIZE_3x3.name
        
        selectedTheme = GameTheme.valueOf(themeName)
        selectedBoardSize = BoardSize.valueOf(boardSizeName)
    }
    
    /** Save current settings to SharedPreferences */
    private fun saveSettings() {
        prefs.edit().apply {
            putString(KEY_THEME, selectedTheme.name)
            putString(KEY_BOARD_SIZE, selectedBoardSize.name)
            apply()
        }
    }
    
    /** Setup dashboard UI elements */
    private fun setupDashboardUI() {
        // Update stats display
        updateStatsDisplay()
        
        // Set current selections
        updateSelectionDisplay()
    }
    
    /** Update the stats display */
    private fun updateStatsDisplay() {
        // Mock stats - in real app, load from persistent storage
        val gamesPlayed = 42
        val gamesWon = 28
        val winRate = if (gamesPlayed > 0) (gamesWon.toFloat() / gamesPlayed) * 100 else 0f
        
        binding.tvStatsGames?.text = "$gamesPlayed"
        binding.tvStatsWins?.text = "$gamesWon"
        binding.tvStatsWinRate?.text = "${winRate.toInt()}%"
    }
    
    /** Update selection display text */
    private fun updateSelectionDisplay() {
        binding.tvSelectedBoardSize?.text = "${selectedBoardSize.size}x${selectedBoardSize.size}"
        binding.tvSelectedDifficulty?.text = selectedDifficulty.displayName
        binding.tvSelectedTheme?.text = selectedTheme.displayName
    }

    override fun onDestroy() {
        super.onDestroy()
        logoFloatAnimator?.cancel()
    }

    // ── Card hover/press scale animations ────────────────────────────────────
    private fun setupCardAnimations() {
        listOf(binding.cardVsBot, binding.cardVsFriend).forEach { card ->
            card.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate()
                        .scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> v.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(OvershootInterpolator(1.5f))
                        .start()
                }
                false // pass event to onClick
            }
        }
        
        // Add animations to settings chips
        listOf(binding.chipTheme, binding.chipBoardSize, binding.chipDifficulty).forEach { chip ->
            chip.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate()
                        .scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> v.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .setInterpolator(OvershootInterpolator(1.4f))
                        .start()
                }
                false
            }
        }
    }

    // ── Button click listeners ───────────────────────────────────────────────
    private fun setupClickListeners() {
        // Bot mode always starts at MEDIUM; GameActivity handles adaptive scaling
        binding.btnStartBot.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnStartBot)
            saveSettings()
            launchGame(GameMode.VS_BOT, selectedDifficulty)
        }
        binding.btnStartFriend.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnStartFriend)
            saveSettings()
            launchGame(GameMode.VS_FRIEND, Difficulty.EASY)
        }
        
        // Settings chip click listeners - open bottom sheets
        binding.chipTheme?.setOnClickListener { showThemeSelector() }
        binding.chipBoardSize?.setOnClickListener { showBoardSizeSelector() }
        binding.chipDifficulty?.setOnClickListener { showDifficultySelector() }
    }
    
    /** Show theme selection bottom sheet */
    private fun showThemeSelector() {
        SoundManager.playButton()
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_theme_selector, null)
        bottomSheet.setContentView(view)
        bottomSheet.show()
        
        // Setup theme options
        val themesContainer = view.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.themesContainer)
        GameTheme.values().forEach { theme ->
            val themeItem = LayoutInflater.from(this).inflate(R.layout.item_theme_option, themesContainer, false)
            val iconText = themeItem.findViewById<TextView>(R.id.tvThemeIcon)
            val nameText = themeItem.findViewById<TextView>(R.id.tvThemeName)
            val colorPreview = themeItem.findViewById<View>(R.id.viewColorPreview)
            
            iconText.text = theme.icon
            nameText.text = theme.displayName
            
            // Set color preview
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.OVAL
            gradientDrawable.setColor(android.graphics.Color.parseColor(theme.primaryColor))
            colorPreview.background = gradientDrawable
            
            themeItem.setOnClickListener {
                selectedTheme = theme
                updateSelectionDisplay()
                applyTheme(theme)
                SoundManager.playButton()
                bottomSheet.dismiss()
            }
            
            themesContainer.addView(themeItem)
        }
    }
    
    /** Show board size selection bottom sheet */
    private fun showBoardSizeSelector() {
        SoundManager.playButton()
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_board_size, null)
        bottomSheet.setContentView(view)
        bottomSheet.show()
        
        BoardSize.values().forEach { size ->
            val sizeOption = view.findViewWithTag<TextView>("size_${size.size}")
            sizeOption?.setOnClickListener {
                selectedBoardSize = size
                updateSelectionDisplay()
                SoundManager.playButton()
                bottomSheet.dismiss()
            }
        }
    }
    
    /** Show difficulty selection bottom sheet */
    private fun showDifficultySelector() {
        SoundManager.playButton()
        val bottomSheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_difficulty, null)
        bottomSheet.setContentView(view)
        bottomSheet.show()
        
        Difficulty.values().forEach { difficulty ->
            val diffOption = view.findViewWithTag<TextView>("diff_${difficulty.name.lowercase()}")
            diffOption?.setOnClickListener {
                selectedDifficulty = difficulty
                updateSelectionDisplay()
                SoundManager.playButton()
                bottomSheet.dismiss()
            }
        }
    }
    
    /** Apply theme colors to UI elements */
    private fun applyTheme(theme: GameTheme) {
        val primaryColor = android.graphics.Color.parseColor(theme.primaryColor)
        val secondaryColor = android.graphics.Color.parseColor(theme.secondaryColor)
        val accentColor = android.graphics.Color.parseColor(theme.accentColor)
        
        // Update accent bar colors
        binding.accentBot?.setBackgroundColor(secondaryColor)
        binding.accentFriend?.setBackgroundColor(primaryColor)
        
        // Update text colors
        binding.tvBotTitle?.setTextColor(secondaryColor)
        binding.tvFriendTitle?.setTextColor(primaryColor)
        binding.tvBotNumber?.setTextColor(secondaryColor)
        binding.tvFriendNumber?.setTextColor(primaryColor)
        
        // Update button backgrounds (would need custom drawables per theme in production)
        // For now, we update the glow effect colors via the GlowAnimator
    }

    private fun launchGame(mode: GameMode, difficulty: Difficulty) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(EXTRA_GAME_MODE,  mode.name)
            putExtra(EXTRA_DIFFICULTY, difficulty.name)
            putExtra(EXTRA_BOARD_SIZE, selectedBoardSize.name)
            putExtra(EXTRA_THEME, selectedTheme.name)
        }
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // ── Entrance stagger animation (vertical arena layout) ───────────────────
    private fun playEntrance() {
        // ── Header: logo + title drop in from top
        listOf(binding.ivLogoLarge, binding.tvTitle).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = -40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(480)
                .setStartDelay((80 + index * 100).toLong())
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (view === binding.ivLogoLarge) {
                        logoFloatAnimator = GlowAnimator.startLogoFloat(binding.ivLogoLarge)
                    }
                }
                .start()
        }

        // ── Bot tile slides up from below, with overshoot
        binding.cardVsBot.apply {
            alpha = 0f
            translationY = 120f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(580)
                .setStartDelay(320)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }

        // ── Friend tile slides up from below (slightly later)
        binding.cardVsFriend.apply {
            alpha = 0f
            translationY = 160f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(580)
                .setStartDelay(440)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }

        // ── VS badge pops in last with a scale punch
        binding.tvVsDivider.apply {
            alpha = 0f
            scaleX = 0.4f
            scaleY = 0.4f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(620)
                .setInterpolator(OvershootInterpolator(2.0f))
                .start()
        }
        
        // ── Settings panel fades in
        listOf(binding.chipTheme, binding.chipBoardSize, binding.chipDifficulty).forEachIndexed { index, chip ->
            chip?.alpha = 0f
            chip?.animate()
                ?.alpha(1f)
                ?.setDuration(300)
                ?.setStartDelay((700 + index * 100).toLong())
                ?.start()
        }
    }
}
