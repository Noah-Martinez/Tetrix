package ch.tetrix.shared

import com.badlogic.gdx.utils.Timer
import ktx.app.KtxInputAdapter

data class KeyHoldConfig(
    /** Sekunden zwischen den Wiederholungen */
    val interval: Float,
    /** Aktion, die bei jedem Intervall ausgeführt wird */
    val onHold: (keycode: Int) -> Unit
)

class KeyHoldSystem(private val keyConfigs: Map<Int, KeyHoldConfig>) : KtxInputAdapter {
    // Laufende Timer-Tasks pro Keycode
    private val tasks = mutableMapOf<Int, Timer.Task>()

    override fun keyDown(keycode: Int): Boolean {
        val config = keyConfigs[keycode]

        if (config == null) {
            return false
        }

        if (!tasks.containsKey(keycode)) {
            val task = object : Timer.Task() {
                override fun run() {
                    config.onHold(keycode)
                }
            }

            // task will be executed immediately first
            Timer.schedule(task, 0f, config.interval)
            tasks[keycode] = task
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        tasks.remove(keycode)?.cancel()
        return keyConfigs.containsKey(keycode)
    }
}
