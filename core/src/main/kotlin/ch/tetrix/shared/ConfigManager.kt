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

    /**
     * Holds and manages the current configuration for the player, encompassing controls for tetromino movement,
     * rotor operations, and audio settings. This variable is mutable only within the class, ensuring controlled
     * updates and consistency in player configuration throughout the application.
     *
     * Modifications to this variable typically occur when saving a new configuration or updating specific control bindings.
     */
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

    /**
     * Saves the provided player configuration to the preference storage.
     * This includes storing integer and float preferences and updating the internal configuration state.
     *
     * @param config The PlayerConfig object containing the player's settings to be saved.
     */
    fun saveConfig(config: PlayerConfig) {
        prefs.run {
            intPrefs.forEach { p -> putInteger(p.key, p.get(config)) }
            floatPrefs.forEach { p -> putFloat(p.key, p.get(config)) }
            flush()
        }
        playerConfig = config
    }

    /**
     * Updates the key binding for a specific preference key to the provided value.
     * Ensures that no other key binding conflicts with the specified value.
     * If a conflict is detected, a `DuplicatedKeyBindingException` is thrown.
     *
     * @param preferencesKey The key of the preference to be updated.
     * @param value The new integer value to bind to the specified preference key.
     * @throws DuplicatedKeyBindingException if the specified value is already bound to another key.
     */
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

    /**
     * Updates the audio-related preference value bounded between 0 and 1, applies the change
     * to the current player configuration, and saves the updated configuration.
     *
     * @param preferencesKey The key representing the audio preference to be changed.
     * @param value The new float value for the audio preference, clamped to a range between 0 and 1.
     */
    fun audioChanged(preferencesKey: String, value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        floatPrefs.forEach { p ->
            if (p.key == preferencesKey) p.set(playerConfig, clamped)
        }
        saveConfig(playerConfig)
    }

    /**
     * Resets the player's configuration to the default settings.
     *
     * This method initializes a new `PlayerConfig` object with
     * default controls and audio settings and saves it using `saveConfig`.
     * It is typically used to revert the player's personalized settings
     * to the original default state.
     */
    fun resetConfig() {
        saveConfig(PlayerConfig())
    }
}
