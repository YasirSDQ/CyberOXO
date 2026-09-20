package cyberoxo.io.model

/** The two players in a Tic-Tac-Toe game. */
enum class Player { X, O }

/** AI difficulty levels. */
enum class Difficulty { EASY, MEDIUM, IMPOSSIBLE }

/** Whether playing against the AI or a local human. */
enum class GameMode { VS_BOT, VS_FRIEND }

/** Snapshot of a game's outcome. */
data class MoveResult(
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val winningLine: List<Int>? = null
) {
    val isOver: Boolean get() = winner != null || isDraw
}
