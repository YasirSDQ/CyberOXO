package cyberoxo.io.model

/** The two players in a Tic-Tac-Toe game. */
enum class Player(val displayName: String) { 
    X("X"), 
    O("O") 
}

/** AI difficulty levels with descriptive names. */
enum class Difficulty(val displayName: String, val description: String) { 
    EASY("Easy", "Random moves - Perfect for beginners"), 
    MEDIUM("Medium", "Smart moves - A balanced challenge"), 
    IMPOSSIBLE("Impossible", "Unbeatable AI - Master level") 
}

/** Whether playing against the AI or a local human. */
enum class GameMode(val displayName: String) { 
    VS_BOT("Vs Bot"), 
    VS_FRIEND("Vs Friend") 
}

/** Board sizes available for different game variants. */
enum class BoardSize(val size: Int, val displayName: String, val winCondition: Int) {
    SIZE_3x3(3, "Classic 3x3", 3),
    SIZE_4x4(4, "Extended 4x4", 4),
    SIZE_5x5(5, "Master 5x5", 4)
}

/** Snapshot of a game's outcome. */
data class MoveResult(
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val winningLine: List<Int>? = null
) {
    val isOver: Boolean get() = winner != null || isDraw
}

/** Game settings bundle for configuration. */
data class GameSettings(
    val gameMode: GameMode = GameMode.VS_FRIEND,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val boardSize: BoardSize = BoardSize.SIZE_3x3,
    val playerXName: String = "Player 1",
    val playerOName: String = "Player 2"
)

/** Statistics tracking for player performance. */
data class GameStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesDrawn: Int = 0,
    val gamesLost: Int = 0,
    val winStreak: Int = 0,
    val bestWinStreak: Int = 0
) {
    val winRate: Float 
        get() = if (gamesPlayed > 0) (gamesWon.toFloat() / gamesPlayed) * 100f else 0f
}
