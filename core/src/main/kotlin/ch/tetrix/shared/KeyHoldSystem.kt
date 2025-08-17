package ch.tetrix.shared

import com.badlogic.gdx.utils.Timer
import ktx.app.KtxInputAdapter

/**
 * Configuration class for handling key hold actions.
 *
 * @property interval Specifies the time interval (in seconds) for invoking the hold action repeatedly while a key is held down.
 * @property onHold A function to be executed repeatedly at the specified interval when a key is held.
 *                  The function receives the keycode of the held key as a parameter.
 */
data class KeyHoldConfig(
    val interval: Float,
    val onHold: (keycode: Int) -> Unit
)

/**
 * System for handling key hold actions with configurable behavior.
 *
 * This class listens for key press and release events, executing a specified action repetitively
 * while a key is held down. The behavior for each key is defined in terms of a set of key configurations.
 * Each configuration includes a time interval and an action to be performed during the key hold.
 *
 * @constructor Creates a KeyHoldSystem with a map of key configurations.
 * @param keyConfigs A map associating keycodes with their respective KeyHoldConfig objects.
 * Each configuration contains the repeat interval and on-hold action for a specific key
 */
class KeyHoldSystem(private val keyConfigs: Map<Int, KeyHoldConfig>) : KtxInputAdapter {
    private val tasks = mutableMapOf<Int, Timer.Task>()

    override fun keyDown(keycode: Int): Boolean {
        val config = keyConfigs[keycode] ?: return false

        if (!tasks.containsKey(keycode)) {
            val task = object : Timer.Task() {
                override fun run() {
                    config.onHold(keycode)
                }
            }

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
