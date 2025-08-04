package ch.tetrix.scoreboard.services

import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.models.ScoreEntity
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.shared.ScoreboardInitException
import ch.tetrix.shared.ScoreboardLoadException
import ch.tetrix.shared.ScoreboardSaveException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object ScoreboardCsvService: ScoreboardRepository {
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

    override fun getGameOverScores(score: Int): List<ScoreDto> {
        try {
            // 1. Read scores from CSV ONCE and add the new player's score.
            val allScores = getAllScores().toMutableList()
            allScores.add(ScoreEntity(score = score))

            // 2. Sort the combined list ONCE and map to DTOs with ranks.
            // This list is now the single source of truth for ranks.
            val rankedScores = allScores
                .sortedByDescending { it.score }
                .mapIndexed { index, entity ->
                    ScoreDto(
                        id = entity.id,
                        username = entity.username,
                        score = entity.score,
                        rank = index + 1 // 1-based rank
                    )
                }

            // 3. Find the player's score in the ranked list.
            val playerIndex = rankedScores.indexOfFirst { it.id == null }

            // 4. Define a window of scores around the player.
            val windowStart = (playerIndex - 2).coerceAtLeast(0)
            val windowEnd = (playerIndex + 2).coerceAtMost(rankedScores.lastIndex)
            val playerContextScores = rankedScores.subList(windowStart, windowEnd + 1)

            // 5. Build the final list: always include the top score, then the player's context.
            val topScore = rankedScores.first()
            val resultList = mutableListOf(topScore)
            resultList.addAll(playerContextScores)

            // 6. Remove any duplicates (if the top score was in the player's context)
            // and limit the final list to 6 items.
            return resultList.distinctBy { it.rank }.take(6)

        } catch (ex: IOException) {
            throw ScoreboardLoadException("Error reading game over scores from CSV: $ex")
        }
    }
}
