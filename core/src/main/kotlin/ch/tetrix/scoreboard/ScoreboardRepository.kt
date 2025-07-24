package ch.tetrix.scoreboard

interface ScoreboardRepository {
    /**
     * Initializes the storage mechanism for the scoreboard.
     * This function sets up the necessary environment (e.g., files or database tables)
     * based on the provided location or defaults if not specified.
     *
     * @param location The optional location for the scoreboard storage. If not provided,
     *                 a default path or configuration is used.
     */
    fun init(location: String = "")

    /**
     * Adds a new score entry with the specified name to the scoreboard.
     *
     * @param name The name to add to the scoreboard.
     * @return `true` if the score was added successfully, `false` otherwise.
     */
    fun addScore(name: String): Boolean
    /**
     * Retrieves the list of scores from the scoreboard storage.
     *
     * @return A list of strings, each representing a score entry.
     *         Returns an empty list if no scores are available or an error occurs.
     */
    fun getScores(): List<String>
}
