package ch.tetrix.shared

import java.nio.file.Path
import java.nio.file.Paths

object AppDataDir {
    /**
     * Returns a per-user, writable data directory across Windows, macOS, and Linux.
     * Example result:
     *  - Windows: C:\Users\you\AppData\Roaming\ch.tetrix\Tetrix\
     *  - macOS: /Users/you/Library/Application Support/Tetrix/
     *  - Linux: /home/you/.local/share/Tetrix/
     */
    fun resolve(appName: String, company: String? = null): Path {
        val os = System.getProperty("os.name").lowercase()
        val home = System.getProperty("user.home")
        val orgAndApp = if (company.isNullOrBlank()) appName else "$company/$appName"

        return when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")
                    ?: Paths.get(home, "AppData", "Roaming").toString()
                Paths.get(appData, orgAndApp.replace('/', '\\'))
            }
            os.contains("mac") || os.contains("darwin") -> {
                Paths.get(home, "Library", "Application Support", appName)
            }
            else -> {
                val xdg = System.getenv("XDG_DATA_HOME")
                    ?: Paths.get(home, ".local", "share").toString()
                Paths.get(xdg, appName)
            }
        }
    }
}
