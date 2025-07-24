package ch.tetrix.scoreboard

import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

object ScoreboardDatabase: ScoreboardRepository {
    private const val DB_FILE_NAME = "scoreboard.db"
    private val PROJECT_ROOT: Path = Paths.get(System.getProperty("user.dir"))
    private val DB_PATH: Path = PROJECT_ROOT.resolve(DB_FILE_NAME)
    private val JDBC_URL = "jdbc:sqlite:${DB_PATH.toAbsolutePath()}"

    private lateinit var conn: Connection

    override fun init(location: String) {
        // only run idempotent statements, since the function is run everytime before connecting to the db
        try {
            val databaseConnection = DriverManager.getConnection(location.takeIf { it.isNotBlank() } ?: JDBC_URL)
            val stmt: Statement = databaseConnection.createStatement()
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS scores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                )
            """.trimIndent()
            )
            conn = databaseConnection
        } catch (error: SQLException) {
            println("An error occurred trying to setup the db: $error")
            throw Exception("Couldn't set up scoreboard database properly, quitting application")
        }
    }

    override fun addScore(name: String): Boolean {
        val sql = "INSERT INTO scores(name) VALUES(?)"

        try {
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, name)
            val result = stmt.executeUpdate()
            return result == 1
        } catch (error: SQLException) {
            println("An error occurred trying to add a score to the db: $error")
            return false
        }
    }

    override fun getScores(): List<String> {
        val sql = "SELECT * FROM scores"

        try {
            val stmt = conn.prepareStatement(sql)
            val result = stmt.executeQuery()

            val names = mutableListOf<String>()
            while (result.next()) {
                // getString by column name (or index 1)
                names += result.getString("name")
            }

            return names
        } catch (error: SQLException) {
            print("An error occurred trying to query the scoreboard db: $error")
            return emptyList()
        }
    }
}
