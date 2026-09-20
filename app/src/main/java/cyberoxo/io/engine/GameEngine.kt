package cyberoxo.io.engine

import cyberoxo.io.model.BoardSize
import cyberoxo.io.model.MoveResult
import cyberoxo.io.model.Player

/**
 * Pure, stateless Tic-Tac-Toe rules engine.
 * Supports multiple board sizes (3x3, 4x4, 5x5).
 * All functions operate on a flat Array<Player?> 
 * where index 0 = top-left (row-major order).
 */
object GameEngine {

    /** Generates all winning combinations for a given board size and win condition. */
    fun getWinCombinations(boardSize: Int, winCondition: Int): List<List<Int>> {
        val combinations = mutableListOf<List<Int>>()
        
        // Horizontal lines
        for (row in 0 until boardSize) {
            for (col in 0 until (boardSize - winCondition + 1)) {
                val line = (0 until winCondition).map { row * boardSize + col + it }
                combinations.add(line)
            }
        }
        
        // Vertical lines
        for (col in 0 until boardSize) {
            for (row in 0 until (boardSize - winCondition + 1)) {
                val line = (0 until winCondition).map { (row + it) * boardSize + col }
                combinations.add(line)
            }
        }
        
        // Diagonal (\) - top-left to bottom-right
        for (row in 0 until (boardSize - winCondition + 1)) {
            for (col in 0 until (boardSize - winCondition + 1)) {
                val line = (0 until winCondition).map { (row + it) * boardSize + col + it }
                combinations.add(line)
            }
        }
        
        // Diagonal (/) - top-right to bottom-left
        for (row in 0 until (boardSize - winCondition + 1)) {
            for (col in (winCondition - 1) until boardSize) {
                val line = (0 until winCondition).map { (row + it) * boardSize + col - it }
                combinations.add(line)
            }
        }
        
        return combinations
    }

    /** Default 3x3 winning combinations for backward compatibility. */
    val WIN_COMBINATIONS_3X3: List<List<Int>> = getWinCombinations(3, 3)

    /**
     * Evaluates the board and returns the current game result.
     * Supports different board sizes and win conditions.
     */
    fun checkResult(board: Array<Player?>, boardSize: BoardSize = BoardSize.SIZE_3x3): MoveResult {
        val combinations = getWinCombinations(boardSize.size, boardSize.winCondition)
        
        for (combo in combinations) {
            val first = board[combo[0]] ?: continue
            if (combo.all { board[it] == first }) {
                return MoveResult(winner = first, winningLine = combo)
            }
        }
        
        if (board.none { it == null }) {
            return MoveResult(isDraw = true)
        }
        
        return MoveResult()
    }

    /** Quick winner check (used by minimax where full result is not needed). */
    fun checkWinner(board: Array<Player?>, boardSize: BoardSize = BoardSize.SIZE_3x3): Player? {
        val combinations = getWinCombinations(boardSize.size, boardSize.winCondition)
        
        for (combo in combinations) {
            val first = board[combo[0]] ?: continue
            if (combo.all { board[it] == first }) {
                return first
            }
        }
        return null
    }

    /** Returns true if all cells are filled and there is no winner. */
    fun isDraw(board: Array<Player?>, boardSize: BoardSize = BoardSize.SIZE_3x3): Boolean =
        board.none { it == null } && checkWinner(board, boardSize) == null
}
