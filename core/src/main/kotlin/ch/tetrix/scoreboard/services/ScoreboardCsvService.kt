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

    override fun addScore(scoreEntity: ScoreEntity): ScoreDto {
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

            return getAllScores().first { it.id == entityToSave.id }

        } catch (ex: IOException) {
            throw ScoreboardSaveException("Fehler beim Hinzufügen des Scores zum CSV: $ex")
        }
    }

    override fun getAllScores(): List<ScoreDto> {
        try {
            return Files.readAllLines(csvPath)
                .drop(1) // skip header
                .mapNotNull { line ->
                    val parts = line.split(',')
                    if (parts.size != 3) return@mapNotNull null

                    ScoreDto(
                        id = parts[0].toLongOrNull(),
                        username = parts[1],
                        score = parts[2].toIntOrNull() ?: 0,
                    )
                }
                .sortedByDescending { it.score }
                .mapIndexed { index, dto ->
                    dto.copy(rank = index + 1)
                }
        } catch (ex: IOException) {
            throw ScoreboardLoadException("Fehler beim Lesen des Scoreboards aus dem CSV: $ex")
        }
    }

    override fun getHighScore(): ScoreDto? {
        return getAllScores().find { it.rank == 1 }?.let {
            ScoreDto(
                id = it.id,
                username = it.username,
                score = it.score,
                rank = it.rank
            )
        }
    }

    override fun getGameOverScores(score: Int): List<ScoreDto> {
        try {
            val allScores = getAllScores().toMutableList()
            allScores.add(ScoreDto(score = score))

            val playerIndex = allScores.indexOfFirst { it.id == null }

            val windowStart = (playerIndex - 2).coerceAtLeast(0)
            val windowEnd = (playerIndex + 2).coerceAtMost(allScores.lastIndex)
            val playerContextScores = allScores.subList(windowStart, windowEnd + 1)

            val topScore = allScores.first()
            val resultList = mutableListOf(topScore)
            resultList.addAll(playerContextScores)

            return resultList.distinctBy { it.rank }.take(6)

        } catch (ex: IOException) {
            throw ScoreboardLoadException("Error reading game over scores from CSV: $ex")
        }
    }
}
