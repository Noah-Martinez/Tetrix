package ch.tetrix.scoreboard.persistence

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
    fun addScore(scoreEntity: ScoreEntity): ScoreEntity

    /**
     * Retrieves the list of scores from the scoreboard storage.
     *
     * @return A list of all Scores.
     *         Returns an empty list if no scores are available.
     * @throws ch.tetrix.shared.ScoreboardLoadException
     */
    fun getAllScores(): List<ScoreEntity>

    /**
     * Retrieves the best ranking score from the scoreboard storage.
     *
     * @return A ScoreEntry or null if no scores are available.
     * @throws ch.tetrix.shared.ScoreboardLoadException
     */
    fun getHighScore(): ScoreEntity?
}
