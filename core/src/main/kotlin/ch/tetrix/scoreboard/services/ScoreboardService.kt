package ch.tetrix.scoreboard.services

import ch.tetrix.scoreboard.repositories.ScoreboardRepository


object ScoreboardService {

    /**
     * Represents the supported storage types for persisting data.
     *
     * This enum is used to specify the type of storage mechanism employed for handling data.
     * It defines the following storage options:
     *
     * - **CSV**: Indicates that data will be stored in a CSV file. Commonly used for simple and portable data storage.
     * - **DATABASE**: Indicates that data will be stored in a database, suitable for scalable, structured, and transactional data management.
     */
    enum class StorageType {
        CSV,
        DATABASE,
    }

    // Map to store scoreboard instances by type
    private val scoreboardRepositoryInstances = mutableMapOf<StorageType, ScoreboardRepository>()


    /**
     * Retrieves a `ScoreboardRepository` instance based on the specified storage type.
     * If an instance for the given type already exists, it will be returned. Otherwise,
     * a new instance will be created, initialized, and cached for future use.
     *
     * @param type The type of storage to use for the scoreboard. This can be either `StorageType.CSV` or `StorageType.DATABASE`.
     * @param location An optional parameter specifying the file path or connection details for the storage.
     * If not provided, a default location will be used.
     * @return The `ScoreboardRepository` instance corresponding to the specified storage type.
     */
    fun getScoreboard(type: StorageType, location: String = ""): ScoreboardRepository {
        scoreboardRepositoryInstances[type]?.let { return it }

        val scoreboard = when (type) {
            StorageType.CSV -> ScoreboardCsvService
            StorageType.DATABASE -> ScoreboardDatabaseService
        }

        scoreboard.init(location)

        scoreboardRepositoryInstances[type] = scoreboard

        return scoreboard
    }
}
