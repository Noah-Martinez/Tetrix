package ch.tetrix.scoreboard

interface Scoreboard {
    /**
     * Prepare the storage (e.g., create tables or files).
     * @param location for DB: JDBC URL or file path; empty means “use default.”
     */
    fun init(location: String = "")

    fun addScore(name: String): Boolean
    fun getScores(): List<String>
}
