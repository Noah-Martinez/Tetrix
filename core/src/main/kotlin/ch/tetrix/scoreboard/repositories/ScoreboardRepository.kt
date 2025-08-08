package ch.tetrix.scoreboard.repositories

import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.models.ScoreEntity

interface ScoreboardRepository {
    /**
     * Initializes the storage mechanism for the scoreboard.
     * This function sets up the necessary environment (e.g., files or database tables)
     * based on the provided location or defaults if not specified.
     *
     * @param location The optional location for the scoreboard storage. If not provided,
     *                 a default path or configuration is used.
     * @throws ch.tetrix.shared.ScoreboardInitException
     */
    fun init(location: String = "")

    /**
     * Adds a new score entry to the scoreboard.
     *
     * @return saved ScoreEntity
     * @throws ch.tetrix.shared.ScoreboardSaveException
     */
    fun addScore(scoreEntity: ScoreEntity): ScoreDto

    /**
     * Retrieves the list of scores from the scoreboard storage.
     *
     * @return A list of all Scores.
     *         Returns an empty list if no scores are available.
     * @throws ch.tetrix.shared.ScoreboardLoadException
     */
    fun getAllScores(): List<ScoreDto>

    /**
     * Retrieves the best ranking score from the scoreboard storage.
     *
     * @return A ScoreEntry or null if no scores are available.
     * @throws ch.tetrix.shared.ScoreboardLoadException
     */
    fun getHighScore(): ScoreDto?

    /**
     * Retrieves a list of game over scores, including the player's score and surrounding scores,
     * ranked in descending order. The result includes the highest score, the player's score,
     * and surrounding scores, up to a total of 6 entries.
     *
     * @param score The score achieved by the player to be included in the ranking.
     * @return A list of ScoreDto objects representing the top score and a context of scores
     *         around the player's score, ordered by rank.
     * @throws ch.tetrix.shared.ScoreboardLoadException If an error occurs during query execution or database access.
     */
    fun getGameOverScores(score: Int): List<ScoreDto>
}
