package ch.tetrix.scoreboard

/**
 * Factory for creating Scoreboard instances.
 * This factory allows the application to use different Scoreboard implementations
 * without being tightly coupled to a specific implementation.
 */
object ScoreboardFactory {
    /**
     * The type of storage to use for the scoreboard.
     */
    enum class StorageType {
        CSV,
        DATABASE,
    }

    // Map to store scoreboard instances by type
    private val scoreboardInstances = mutableMapOf<StorageType, Scoreboard>()

    /**
     * Gets a Scoreboard instance of the specified type.
     * If a Scoreboard of the specified type has already been created, returns that instance.
     *
     * @param type The type of storage to use for the scoreboard.
     * @param location Optional location for the scoreboard storage.
     * @return A Scoreboard instance of the specified type.
     */
    fun getScoreboard(type: StorageType, location: String = ""): Scoreboard {
        // If we already have a scoreboard instance of this type, return it
        scoreboardInstances[type]?.let { return it }

        // Otherwise, create a new instance based on the specified type
        val scoreboard = when (type) {
            StorageType.CSV -> ScoreboardCsv
            StorageType.DATABASE -> ScoreboardDatabase
        }

        // Initialize the scoreboard
        scoreboard.init(location)

        // Store the instance for future use
        scoreboardInstances[type] = scoreboard

        return scoreboard
    }
}
