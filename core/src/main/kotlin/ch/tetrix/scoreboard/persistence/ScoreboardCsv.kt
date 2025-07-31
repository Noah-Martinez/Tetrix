package ch.tetrix.scoreboard.persistence

import ch.tetrix.shared.ScoreboardInitException
import ch.tetrix.shared.ScoreboardLoadException
import ch.tetrix.shared.ScoreboardSaveException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object ScoreboardCsv: ScoreboardRepository {
    private const val DEFAULT_CSV = "scoreboard.csv"
    private val PROJECT_ROOT: Path = Paths.get(System.getProperty("user.dir"))
    private const val HEADER = "id,username,score"

    private lateinit var csvPath: Path

    override fun init(location: String) {
        csvPath = Paths.get(location.takeIf { it.isNotBlank() } ?: PROJECT_ROOT.resolve(DEFAULT_CSV).toString())

        try {
            csvPath.parent?.let { Files.createDirectories(it) }

            if (Files.notExists(csvPath)) {
                Files.write(csvPath, listOf(HEADER), StandardOpenOption.CREATE)
            }
        } catch (ex: IOException) {
            throw ScoreboardInitException("Initialisierung des CSV-Scoreboards fehlgeschlagen: $ex")
        }
    }

    override fun addScore(scoreEntity: ScoreEntity): ScoreEntity {
        try {
            val allScores = getAllScores()
            val nextId = (allScores.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1L

            val entityToSave = scoreEntity.copy(id = nextId)

            val csvLine = "${entityToSave.id},${entityToSave.username},${entityToSave.score}"

            Files.write(
                csvPath,
                listOf(csvLine),
                StandardOpenOption.APPEND
            )

            return entityToSave
        } catch (ex: IOException) {
            throw ScoreboardSaveException("Fehler beim Hinzufügen des Scores zum CSV: $ex")
        }
    }

    override fun getAllScores(): List<ScoreEntity> {
        try {
            return Files.readAllLines(csvPath)
                .drop(1) // skip header
                .mapNotNull { line ->
                    val parts = line.split(',')
                    if (parts.size != 3) {
                        null
                    }

                    ScoreEntity(
                        id = parts[0].toLongOrNull(),
                        username = parts[1],
                        score = parts[2].toIntOrNull() ?: 0
                    )
                }
        } catch (ex: IOException) {
            throw ScoreboardLoadException("Fehler beim Lesen des Scoreboards aus dem CSV: $ex")
        }
    }

    override fun getHighScore(): ScoreEntity? {
        return getAllScores().maxByOrNull { it.score }
    }
}
