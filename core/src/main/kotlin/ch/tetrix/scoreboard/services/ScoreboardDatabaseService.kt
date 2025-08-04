package ch.tetrix.scoreboard.services

import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.models.ScoreEntity
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.shared.ScoreboardInitException
import ch.tetrix.shared.ScoreboardLoadException
import ch.tetrix.shared.ScoreboardSaveException
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import ktx.log.logger

object ScoreboardDatabaseService : ScoreboardRepository {
    private const val DB_FILE_NAME = "scoreboard.db"
    private val PROJECT_ROOT: Path = Paths.get(System.getProperty("user.dir"))
    private val DB_PATH: Path = PROJECT_ROOT.resolve(DB_FILE_NAME)
    private val JDBC_URL = "jdbc:sqlite:${DB_PATH.toAbsolutePath()}"

    private lateinit var conn: Connection
    private var log = logger<ScoreboardDatabaseService>()

    override fun init(location: String) {
        // only run idempotent statements, since the function is run everytime before connecting to the db

        log.info { "Initializing database connection: $PROJECT_ROOT" }

        try {
            val databaseConnection = DriverManager.getConnection(location.takeIf { it.isNotBlank() } ?: JDBC_URL)
            val stmt: Statement = databaseConnection.createStatement()
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS scores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    score INT NOT NULL
                )
            """.trimIndent()
            )
            conn = databaseConnection
        } catch (error: SQLException) {
            throw ScoreboardInitException("An error occurred trying to setup the db: $error")
        }
    }

    override fun addScore(scoreEntity: ScoreEntity): ScoreEntity {
        val sql = "INSERT INTO scores(username, score) VALUES(?, ?)"

        try {
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) // Enable generated keys
            stmt.setString(1, scoreEntity.username)
            stmt.setInt(2, scoreEntity.score)

            val rowsAffected = stmt.executeUpdate()
            if (rowsAffected == 0) {
                throw ScoreboardSaveException("Unable to add score $scoreEntity to the db")
            }

            stmt.generatedKeys.use { generatedKeys ->
                if (generatedKeys.next()) {
                    val newId = generatedKeys.getLong(1)
                    // Optional: log.debug("New score ID generated: $newId")
                    return scoreEntity.copy(id = newId)
                } else {
                    throw ScoreboardSaveException("Couldn't retrieve the generated key from the db")
                }
            }
        } catch (error: SQLException) {
            throw ScoreboardSaveException("An error occurred trying to add a score to the db: $error")
        }
    }

    override fun getAllScores(): List<ScoreEntity> {
        val sql = "SELECT * FROM scores ORDER BY score DESC"

        try {
            val stmt = conn.prepareStatement(sql)
            val result = stmt.executeQuery()

            val scores = mutableListOf<ScoreEntity>()
            while (result.next()) {
                scores += ScoreEntity(
                    result.getLong("id"),
                    result.getString("username"),
                    result.getInt("score")
                )
            }

            return scores
        } catch (error: SQLException) {
            throw ScoreboardLoadException("An error occurred trying to query the scoreboard db: $error")
        }
    }

    override fun getHighScore(): ScoreEntity? {
        val sql = "SELECT * FROM scores ORDER BY score DESC LIMIT 1"

        try {
            val stmt = conn.prepareStatement(sql)
            val result = stmt.executeQuery()

            if (!result.next()) return null

            return ScoreEntity(
                result.getLong("id"),
                result.getString("username"),
                result.getInt("score")
            )
        } catch (error: SQLException) {
            throw ScoreboardLoadException("An error occurred trying to query the scoreboard db: $error")
        }
    }

    override fun getGameOverScores(score: Int): List<ScoreDto> {
        val sql = """
        WITH ranked_scores AS (
            SELECT
                id,
                username,
                score,
                ROW_NUMBER() OVER (ORDER BY score DESC) as rank
            FROM (
                SELECT id, username, score FROM scores
                UNION ALL
                SELECT NULL AS id, '' AS username, ? AS score
            )
        )
        SELECT id, username, score, rank FROM ranked_scores WHERE rank = 1
        UNION
        SELECT id, username, score, rank FROM ranked_scores
        WHERE rank BETWEEN
            (SELECT rank FROM ranked_scores WHERE id IS NULL) - 2
            AND
            (SELECT rank FROM ranked_scores WHERE id IS NULL) + 2
        ORDER BY rank
        LIMIT 6
        """.trimIndent()

        try {
            val stmt = conn.prepareStatement(sql)
            stmt.setInt(1, score)

            val result = stmt.executeQuery()

            val scores = mutableListOf<ScoreDto>()
            while (result.next()) {
                scores += ScoreDto(
                    // The id column can be null for the player's transient score
                    id = result.getObject("id")?.let { (it as Number).toLong() },
                    username = result.getString("username"),
                    score = result.getInt("score"),
                    rank = result.getInt("rank")
                )
            }
            return scores
        } catch (error: SQLException) {
            throw ScoreboardLoadException("An error occurred trying to query game over scores from the db: $error")
        }
    }
}
