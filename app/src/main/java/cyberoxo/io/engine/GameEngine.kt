package cyberoxo.io.engine

import cyberoxo.io.model.MoveResult
import cyberoxo.io.model.Player

/**
 * Pure, stateless Tic-Tac-Toe rules engine.
 * All functions operate on a flat Array<Player?> of size 9
 * where index 0 = top-left, 8 = bottom-right (row-major order).
 */
object GameEngine {

    /** All 8 winning combinations (row, column, diagonal). */
    val WIN_COMBINATIONS: List<List<Int>> = listOf(
        listOf(0, 1, 2), // top row
        listOf(3, 4, 5), // middle row
        listOf(6, 7, 8), // bottom row
        listOf(0, 3, 6), // left column
        listOf(1, 4, 7), // center column
        listOf(2, 5, 8), // right column
        listOf(0, 4, 8), // diagonal \
        listOf(2, 4, 6)  // diagonal /
    )

    /**
     * Evaluates the board and returns the current game result.
     * Returns a [MoveResult] with winner, draw flag, and winning line indices.
     */
    fun checkResult(board: Array<Player?>): MoveResult {
        for (combo in WIN_COMBINATIONS) {
            val a = combo[0]; val b = combo[1]; val c = combo[2]
            if (board[a] != null && board[a] == board[b] && board[b] == board[c]) {
                return MoveResult(winner = board[a], winningLine = combo)
            }
        }
        if (board.none { it == null }) {
            return MoveResult(isDraw = true)
        }
        return MoveResult()
    }

    /** Quick winner check (used by minimax where full result is not needed). */
    fun checkWinner(board: Array<Player?>): Player? {
        for (combo in WIN_COMBINATIONS) {
            val a = combo[0]; val b = combo[1]; val c = combo[2]
            if (board[a] != null && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }
        return null
    }

    /** Returns true if all cells are filled and there is no winner. */
    fun isDraw(board: Array<Player?>): Boolean =
        board.none { it == null } && checkWinner(board) == null
}
