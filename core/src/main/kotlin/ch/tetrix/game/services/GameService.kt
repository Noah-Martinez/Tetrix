package ch.tetrix.game.services

import ch.tetrix.shared.BehaviorSignal
import kotlin.math.pow
import ktx.log.logger

object GameService {
    private val log = logger<GameService>()

    val highScore = BehaviorSignal(0)
    val score = BehaviorSignal(0)
    val level = BehaviorSignal(0)
    val squares = BehaviorSignal(0)
    val lines = BehaviorSignal(0)

    /**
     * Represents the active status of the game.
     *
     * This signal indicates whether a game is currently in an active session or not.
     * Its value is updated throughout the game's lifecycle based on events like starting, pausing, resuming, or ending a game.
     *
     * - `true` means the game is active and playable.
     * - `false` means the game is either paused, over, or reset.
     */
    val isGameActive = BehaviorSignal(false)
    /**
     * Represents the paused state of the game.
     *
     * It is a signal that tracks whether the game is currently paused. A value of `true` indicates the game is paused,
     * while `false` indicates the game is running. This state can be monitored and updated during gameplay, controlling
     * the game's flow and associated functionalities.
     *
     * Used by multiple game control functions to pause, resume, start or end the game, and also to manage gameplay updates
     * such as play time tracking.
     */
    val isGamePaused = BehaviorSignal(false)
    /**
     * Represents the state of whether the game is over or not.
     * It is a reactive signal that gets updated through gameplay events such as resetting,
     * ending the game, or transitioning between game states.
     *
     * The initial value is set to `false`, indicating that the game has not ended.
     * This variable is updated by dispatching new values to control the flow of the game.
     *
     * Usage scenarios include:
     * - Checking if the game is over before performing certain actions like pausing or resuming.
     * - Updating other game logic and UI elements when the game ends.
     */
    val isGameOver = BehaviorSignal(false)

    val totalPlayTime = BehaviorSignal(0L) // in milliseconds
    val gamesPlayed = BehaviorSignal(0)

    val linesRequiredForNextLevel = BehaviorSignal(10)
    val fallSpeed = BehaviorSignal(1000L) // milliseconds between drops

    private const val BASE_POINTS_PER_CUBES = 100
    private const val LEVEL_MULTIPLIER = 1.2f

    init {
        reset()
        score.dispatch(10) //TODO REMOVE THIS ON PRODUCTION
        highScore.dispatch(123)
    }

    /**
     * Starts a new game
     */
    fun startNewGame() {
        log.info { "Starting new game" }
        reset()
        isGameActive.dispatch(true)
        isGameOver.dispatch(false)
        isGamePaused.dispatch(false)
        gamesPlayed.dispatch(gamesPlayed.value + 1)
    }

    /**
     * Pauses the current game
     */
    fun pauseGame() {
        if (isGameActive.value && !isGameOver.value) {
            log.info { "Game paused" }
            isGamePaused.dispatch(true)
        }
    }

    /**
     * Resumes the paused game
     */
    fun resumeGame() {
        if (isGameActive.value && !isGameOver.value) {
            log.info { "Game resumed" }
            isGamePaused.dispatch(false)
        }
    }

    /**
     * Ends the current game
     */
    fun endGame() {
        log.info { "Game ended. Final score: ${score.value}" }
        isGameActive.dispatch(false)
        isGameOver.dispatch(true)
        isGamePaused.dispatch(false)

        // Update high score if necessary
        if (score.value > highScore.value) {
            log.info { "New high score: ${score.value}" }
            highScore.dispatch(score.value)
        }
    }

    /**
     * Resets all game values to initial state
     */
    fun reset() {
        score.dispatch(0)
        level.dispatch(1)
        squares.dispatch(0)
        lines.dispatch(0)
        isGameActive.dispatch(false)
        isGamePaused.dispatch(false)
        isGameOver.dispatch(false)
        linesRequiredForNextLevel.dispatch(10)
        updateFallSpeed()
    }

    /**
     * Adds points to the current score
     */
    fun addScore(points: Int) {
        val newScore = score.value + points
        score.dispatch(newScore)
        log.debug { "Score updated: $newScore (+$points)" }
    }

    /**
     * Increments the squares (pieces) counter
     */
    fun incrementSquares() {
        val newSquares = squares.value + 1
        squares.dispatch(newSquares)
        log.debug { "Squares placed: $newSquares" }
    }

    /**
     * Adds cleared lines and handles level progression
     */
    fun addLines(linesCleared: Int) {
        if (linesCleared <= 0) return

        val newLines = lines.value + linesCleared
        lines.dispatch(newLines)

        // Calculate score bonus based on lines cleared simultaneously
        val scoreBonus = calculateLineScore(linesCleared)
        addScore(scoreBonus)

        // Check for level progression
        checkLevelProgression()

        log.info { "Lines cleared: $linesCleared, Total lines: $newLines, Score bonus: $scoreBonus" }
    }

    /**
     * Updates play time (should be called regularly during active gameplay)
     */
    fun updatePlayTime(deltaMs: Long) {
        if (isGameActive.value && !isGamePaused.value) {
            totalPlayTime.dispatch(totalPlayTime.value + deltaMs)
        }
    }

    /**
     * Calculates score based on lines cleared simultaneously
     */
    private fun calculateLineScore(linesCleared: Int): Int {
        val baseScore = when (linesCleared) {
            1 -> BASE_POINTS_PER_CUBES
            2 -> BASE_POINTS_PER_CUBES * 3
            3 -> BASE_POINTS_PER_CUBES * 5
            4 -> BASE_POINTS_PER_CUBES * 8 // Tetris bonus
            else -> BASE_POINTS_PER_CUBES * linesCleared
        }

        return (baseScore * LEVEL_MULTIPLIER.toDouble().pow((level.value - 1).toDouble())).toInt()
    }

    /**
     * Checks if player has progressed to next level
     */
    private fun checkLevelProgression() {
        val currentLevel = level.value
        val totalLines = lines.value
        val requiredLines = currentLevel * 10 // 10 lines per level

        if (totalLines >= requiredLines) {
            val newLevel = currentLevel + 1
            level.dispatch(newLevel)
            linesRequiredForNextLevel.dispatch((newLevel * 10) - totalLines)
            updateFallSpeed()

            log.info { "Level up! New level: $newLevel" }
        } else {
            linesRequiredForNextLevel.dispatch(requiredLines - totalLines)
        }
    }

    /**
     * Updates fall speed based on current level
     */
    private fun updateFallSpeed() {
        val currentLevel = level.value
        val newSpeed = 50L.coerceAtLeast(1000L - (currentLevel - 1) * 50L)
        fallSpeed.dispatch(newSpeed)
        log.debug { "Fall speed updated to ${newSpeed}ms for level $currentLevel" }
    }

    /**
     * Gets current game statistics
     */
    fun getGameStats(): GameStats {
        return GameStats(
            score = score.value,
            level = level.value,
            lines = lines.value,
            squares = squares.value,
            playTimeMs = totalPlayTime.value,
            gamesPlayed = gamesPlayed.value,
            highScore = highScore.value,
            fallSpeed = fallSpeed.value
        )
    }

    /**
     * Data class for game statistics
     */
    data class GameStats(
        val score: Int,
        val level: Int,
        val lines: Int,
        val squares: Int,
        val playTimeMs: Long,
        val gamesPlayed: Int,
        val highScore: Int,
        val fallSpeed: Long
    ) {
        val playTimeSeconds: Long get() = playTimeMs / 1000
        val playTimeMinutes: Long get() = playTimeSeconds / 60
        val averageScorePerGame: Float get() = if (gamesPlayed > 0) score.toFloat() / gamesPlayed else 0f
    }
}
