package ch.tetrix.scoreboard.repositories

import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.models.ScoreEntity

interface ScoreboardRepository {
    /**
     * Initializes the scoreboard with the provided location for storage.
     * If the location is not specified, a default location is used.
     *
     * @param location Optional file path for storing the scoreboard data.
     * If the path is empty, a default location is chosen.
     */
    fun init(location: String = "")

    /**
     * Adds a new score to the scoreboard.
     *
     * @param scoreEntity An object containing the score details, such as the user's name, score value, and optional ID.
     * @return A `ScoreDto` object representing the saved score with additional details such as rank.
     */
    fun addScore(scoreEntity: ScoreEntity): ScoreDto

    /**
     * Retrieves a list of all scores from the scoreboard.
     *
     * @return A list of `ScoreDto` objects representing all scores currently stored.
     */
    fun getAllScores(): List<ScoreDto>

    /**
     * Retrieves the highest score from the scoreboard storage.
     *
     * @return The highest score as a `ScoreDto` object, or `null` if no scores are available.
     */
    fun getHighScore(): ScoreDto?

    /**
     * Retrieves a list of `ScoreDto` objects to display at the game-over screen.
     * The list is typically filtered or sorted based on the provided score, representing
     * the player's score in the current game session.
     *
     * @param score The player's score from the game session, used to determine relevant scores for display.
     * @return A list of `ScoreDto` objects to be shown on the game-over leaderboard.
     */
    fun getGameOverScores(score: Int): List<ScoreDto>
}
