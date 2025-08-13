package ch.tetrix.shared

import ch.tetrix.shared.models.AudioControls
import ch.tetrix.shared.models.PlayerConfig
import ch.tetrix.shared.models.RotorControls
import ch.tetrix.shared.models.TetrominoControls
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

object ConfigManager {
    private const val PREF_NAME = "tetrix_player_config"
    private val prefs: Preferences = Gdx.app.getPreferences(PREF_NAME)

    var playerConfig: PlayerConfig
        private set

    private data class IntPref(
        val key: String,
        val get: (PlayerConfig) -> Int,
        val set: (PlayerConfig, Int) -> Unit
    )

    private val intPrefs = listOf(
        IntPref(TetrominoControls.KEY_MOVE_UP, { it.tetromino.moveUp }, { c, v -> c.tetromino.moveUp = v }),
        IntPref(TetrominoControls.KEY_MOVE_DOWN, { it.tetromino.moveDown }, { c, v -> c.tetromino.moveDown = v }),
        IntPref(TetrominoControls.KEY_MOVE_LEFT, { it.tetromino.moveLeft }, { c, v -> c.tetromino.moveLeft = v }),
        IntPref(TetrominoControls.KEY_MOVE_RIGHT, { it.tetromino.moveRight }, { c, v -> c.tetromino.moveRight = v }),
        IntPref(TetrominoControls.KEY_SNAP, { it.tetromino.snap }, { c, v -> c.tetromino.snap = v }),
        IntPref(TetrominoControls.KEY_ROTATE_LEFT, { it.tetromino.rotateLeft }, { c, v -> c.tetromino.rotateLeft = v }),
        IntPref(TetrominoControls.KEY_ROTATE_RIGHT, { it.tetromino.rotateRight }, { c, v -> c.tetromino.rotateRight = v }),

        IntPref(RotorControls.KEY_ROTATE_LEFT, { it.rotor.rotateLeft }, { c, v -> c.rotor.rotateLeft = v }),
        IntPref(RotorControls.KEY_ROTATE_RIGHT, { it.rotor.rotateRight }, { c, v -> c.rotor.rotateRight = v }),
    )

    private data class FloatPref(
        val key: String,
        val get: (PlayerConfig) -> Float,
        val set: (PlayerConfig, Float) -> Unit
    )

    private val floatPrefs = listOf(
        FloatPref(AudioControls.MUSIC, { it.audio.musicVolume }, { c, v -> c.audio.musicVolume = v }),
        FloatPref(AudioControls.SOUND, { it.audio.soundVolume }, { c, v -> c.audio.soundVolume = v }),
    )

    init {
        playerConfig = loadConfig()
    }

    private fun loadConfig(): PlayerConfig {
        val config = PlayerConfig()

        intPrefs.forEach { p ->
            val defaultValue = p.get(config)
            p.set(config, prefs.getInteger(p.key, defaultValue))
        }
        floatPrefs.forEach { p ->
            val defaultValue = p.get(config)
            p.set(config, prefs.getFloat(p.key, defaultValue))
        }

        if (prefs.get().isEmpty()) saveConfig(config)
        return config
    }

    fun saveConfig(config: PlayerConfig) {
        prefs.run {
            intPrefs.forEach { p -> putInteger(p.key, p.get(config)) }
            floatPrefs.forEach { p -> putFloat(p.key, p.get(config)) }
            flush()
        }
        playerConfig = config
    }

    fun keyBindingChanged(preferencesKey: String, value: Int) {
        intPrefs.forEach { p ->
            if (p.key != preferencesKey && p.get(playerConfig) == value) {
                val key = p.get(playerConfig)
                throw DuplicatedKeyBindingException("KeyBinding $preferencesKey is already bound to $key", key)
            }
        }
        intPrefs.forEach { p ->
            if (p.key == preferencesKey) p.set(playerConfig, value)
        }
        saveConfig(playerConfig)
    }

    fun audioChanged(preferencesKey: String, value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        floatPrefs.forEach { p ->
            if (p.key == preferencesKey) p.set(playerConfig, clamped)
        }
        saveConfig(playerConfig)
    }

    fun resetConfig() {
        saveConfig(PlayerConfig())
    }
}
