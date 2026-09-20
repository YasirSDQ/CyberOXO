package cyberoxo.io.engine

import cyberoxo.io.model.BoardSize
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
    fun getBotMove(board: Array<Player?>, difficulty: Difficulty, boardSize: BoardSize = BoardSize.SIZE_3x3): Int = when (difficulty) {
        Difficulty.EASY       -> easyMove(board)
        Difficulty.MEDIUM     -> mediumMove(board, boardSize)
        Difficulty.IMPOSSIBLE -> bestMove(board, boardSize)
    }

    // ── Easy: pure random ───────────────────────────────────────────────────
    private fun easyMove(board: Array<Player?>): Int =
        board.indices.filter { board[it] == null }.random()

    // ── Medium: heuristic ───────────────────────────────────────────────────
    private fun mediumMove(board: Array<Player?>, boardSize: BoardSize): Int {
        // 1. Winning move for bot
        findWinningMove(board, Player.O, boardSize)?.let { return it }
        // 2. Block player from winning
        findWinningMove(board, Player.X, boardSize)?.let { return it }
        // 3. Take centre (for 3x3) or center area
        val center = if (boardSize.size == 3) 4 else (boardSize.size * boardSize.size) / 2
        if (board[center] == null) return center
        // 4. Take a corner
        val corners = listOf(0, boardSize.size - 1, 
                            boardSize.size * (boardSize.size - 1), 
                            boardSize.size * boardSize.size - 1)
        corners.filter { board[it] == null }.randomOrNull()?.let { return it }
        // 5. Fallback random
        return easyMove(board)
    }

    /** Returns the index that completes a winning trio for [player], or null. */
    private fun findWinningMove(board: Array<Player?>, player: Player, boardSize: BoardSize): Int? {
        val combinations = GameEngine.getWinCombinations(boardSize.size, boardSize.winCondition)
        
        for (combo in combinations) {
            val cells = combo.map { board[it] }
            if (cells.count { it == player } == boardSize.winCondition - 1 && 
                cells.count { it == null } == 1) {
                return combo.firstOrNull { board[it] == null }
            }
        }
        return null
    }

    // ── Impossible: Minimax ─────────────────────────────────────────────────
    private fun bestMove(board: Array<Player?>, boardSize: BoardSize): Int {
        var bestScore = Int.MIN_VALUE
        var bestIndex = -1
        val combinations = GameEngine.getWinCombinations(boardSize.size, boardSize.winCondition)
        
        for (i in board.indices) {
            if (board[i] == null) {
                board[i] = Player.O
                val score = minimax(board, depth = 0, isMaximizing = false, boardSize, combinations)
                board[i] = null
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = i
                }
            }
        }
        return if (bestIndex >= 0) bestIndex else easyMove(board)
    }

    /**
     * Minimax recursive search.
     * Bot (O) is the maximising player; human (X) is the minimising player.
     * Score: +10 for O win, -10 for X win, 0 for draw (depth subtracted to prefer faster wins).
     */
    private fun minimax(board: Array<Player?>, depth: Int, isMaximizing: Boolean, 
                       boardSize: BoardSize, combinations: List<List<Int>>): Int {
        val winner = checkWinnerFast(board, combinations)
        when (winner) {
            Player.O -> return 10 - depth
            Player.X -> return depth - 10
            else     -> Unit
        }
        if (board.none { it == null }) return 0

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = Player.O
                    best = maxOf(best, minimax(board, depth + 1, false, boardSize, combinations))
                    board[i] = null
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    board[i] = Player.X
                    best = minOf(best, minimax(board, depth + 1, true, boardSize, combinations))
                    board[i] = null
                }
            }
            best
        }
    }
    
    /** Fast winner check using pre-computed combinations */
    private fun checkWinnerFast(board: Array<Player?>, combinations: List<List<Int>>): Player? {
        for (combo in combinations) {
            val first = board[combo[0]] ?: continue
            if (combo.all { board[it] == first }) {
                return first
            }
        }
        return null
    }
}
