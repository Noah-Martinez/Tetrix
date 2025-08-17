package ch.tetrix.game

import ch.tetrix.shared.models.PlayerConfig
import com.badlogic.gdx.InputAdapter

/**
 * Controller that handles player input and maps key presses to specific game actions.
 *
 * @constructor Creates a new instance of GameInputController.
 * @param config the configuration of player controls specifying the mappings for the tetromino and rotor actions.
 * @param onAction a callback that is invoked with an appropriate [Action] whenever a corresponding key input is detected.
 */
class GameInputController(
    private val config: PlayerConfig,
    private val onAction: (Action) -> Unit
) : InputAdapter() {

    // Note: We intentionally do NOT emit MoveLeft/MoveRight here to avoid double-steps,
    // since left/right are handled by KeyHoldSystem for smooth repeat.
    enum class Action { MoveUp, MoveDown, Snap, RotLeft, RotRight, RotorLeft, RotorRight, FastFallOff }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            config.tetromino.moveUp -> onAction(Action.MoveUp)
            config.tetromino.moveDown -> onAction(Action.MoveDown)
            config.tetromino.snap -> onAction(Action.Snap)
            config.tetromino.rotateLeft -> onAction(Action.RotLeft)
            config.tetromino.rotateRight -> onAction(Action.RotRight)
            config.rotor.rotateLeft -> onAction(Action.RotorLeft)
            config.rotor.rotateRight -> onAction(Action.RotorRight)
            else -> return false
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return if (keycode == config.tetromino.moveUp || keycode == config.tetromino.moveDown) {
            onAction(Action.FastFallOff)
            true
        } else {
            false
        }
    }
}
