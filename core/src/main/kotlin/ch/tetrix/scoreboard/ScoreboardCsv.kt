package ch.tetrix.scoreboard

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object ScoreboardCsv: ScoreboardRepository {
    private const val DEFAULT_CSV = "scoreboard.csv"
    private val PROJECT_ROOT: Path = Paths.get(System.getProperty("user.dir"))
    private val CSV_PATH: Path get() = PROJECT_ROOT.resolve(DEFAULT_CSV)

    override fun init(location: String) {
        val path = Paths.get(location.takeIf { it.isNotBlank() } ?: CSV_PATH.toString())
        try {
            Files.createDirectories(path.parent)
            if (Files.notExists(path)) {
                Files.write(path, listOf("name"), StandardOpenOption.CREATE)
            }
        } catch (ex: IOException) {
            println("CSV init error: $ex")
            throw Exception("Couldn't initialize CSV scoreboard", ex)
        }
    }

    override fun addScore(name: String): Boolean =
        try {
            Files.write(
                CSV_PATH,
                listOf(name),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
            true
        } catch (ex: IOException) {
            println("CSV addScore error: $ex")
            false
        }

    override fun getScores(): List<String> =
        try {
            Files.readAllLines(CSV_PATH).let { lines ->
                if (lines.firstOrNull() == "name") lines.drop(1) else lines
            }
        } catch (ex: IOException) {
            println("CSV getScoreboard error: $ex")
            emptyList()
        }

}
