package cyberoxo.io.engine

import cyberoxo.io.model.Difficulty
import cyberoxo.io.model.Player

/**
 * AI move selector for "Vs Bot" mode.
 *
 * Bot always plays as [Player.O].
 *   - EASY      → random empty cell
 *   - MEDIUM    → win if possible → block player win → centre → corner → random
 *   - IMPOSSIBLE → full Minimax (never loses, never misses a win)
 */
object BotEngine {

    /**
     * Returns the board index (0-8) the bot should play.
     * Caller is responsible for validating that there are still empty cells.
     */
    fun getBotMove(board: Array<Player?>, difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY       -> easyMove(board)
        Difficulty.MEDIUM     -> mediumMove(board)
        Difficulty.IMPOSSIBLE -> bestMove(board)
    }

    // ── Easy: pure random ───────────────────────────────────────────────────
    private fun easyMove(board: Array<Player?>): Int =
        board.indices.filter { board[it] == null }.random()

    // ── Medium: heuristic ───────────────────────────────────────────────────
    private fun mediumMove(board: Array<Player?>): Int {
        // 1. Winning move for bot
        findWinningMove(board, Player.O)?.let { return it }
        // 2. Block player from winning
        findWinningMove(board, Player.X)?.let { return it }
        // 3. Take centre
        if (board[4] == null) return 4
        // 4. Take a corner
        listOf(0, 2, 6, 8).filter { board[it] == null }.randomOrNull()?.let { return it }
        // 5. Fallback random
        return easyMove(board)
    }

    /** Returns the index that completes a winning trio for [player], or null. */
    private fun findWinningMove(board: Array<Player?>, player: Player): Int? {
        for (combo in GameEngine.WIN_COMBINATIONS) {
            val (a, b, c) = Triple(combo[0], combo[1], combo[2])
            val cells = listOf(board[a], board[b], board[c])
            if (cells.count { it == player } == 2 && cells.count { it == null } == 1) {
                return when {
                    board[a] == null -> a
                    board[b] == null -> b
                    else             -> c
                }
            }
        }
        return null
    }

    // ── Impossible: Minimax ─────────────────────────────────────────────────
    private fun bestMove(board: Array<Player?>): Int {
        var bestScore = Int.MIN_VALUE
        var bestIndex = -1
        for (i in board.indices) {
            if (board[i] == null) {
                board[i] = Player.O
                val score = minimax(board, depth = 0, isMaximizing = false)
                board[i] = null
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = i
                }
            }
        }
        return bestIndex
    }

    /**
     * Minimax recursive search.
     * Bot (O) is the maximising player; human (X) is the minimising player.
     * Score: +10 for O win, -10 for X win, 0 for draw (depth subtracted to prefer faster wins).
     */
    private fun minimax(board: Array<Player?>, depth: Int, isMaximizing: Boolean): Int {
        when (GameEngine.checkWinner(board)) {
            Player.O -> return 10 - depth
            Player.X -> return depth - 10
            else     -> Unit
        }
        if (GameEngine.isDraw(board)) return 0

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = Player.O
                    best = maxOf(best, minimax(board, depth + 1, false))
                    board[i] = null
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = Player.X
                    best = minOf(best, minimax(board, depth + 1, true))
                    board[i] = null
                }
            }
            best
        }
    }
}
