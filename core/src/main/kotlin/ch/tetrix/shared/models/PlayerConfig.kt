package ch.tetrix.shared.models

import com.badlogic.gdx.Input

data class PlayerConfig(
    val tetromino: TetrominoControls = TetrominoControls(),
    val rotor: RotorControls = RotorControls()
)

data class TetrominoControls(
    var moveUp: Int = Input.Keys.W,
    var moveDown: Int = Input.Keys.S,
    var moveLeft: Int = Input.Keys.A,
    var moveRight: Int = Input.Keys.D,
    var snap: Int = Input.Keys.SPACE,
    var rotateLeft: Int = Input.Keys.Q,
    var rotateRight: Int = Input.Keys.E,
) {
    companion object {
        const val KEY_MOVE_UP = "tetromino.moveUp"
        const val KEY_MOVE_DOWN = "tetromino.moveDown"
        const val KEY_MOVE_LEFT = "tetromino.moveLeft"
        const val KEY_MOVE_RIGHT = "tetromino.moveRight"
        const val KEY_SNAP = "tetromino.snap"
        const val KEY_ROTATE_LEFT = "tetromino.rotateLeft"
        const val KEY_ROTATE_RIGHT = "tetromino.rotateRight"
    }
}

data class RotorControls(
    var rotateLeft: Int = Input.Keys.J,
    var rotateRight: Int = Input.Keys.L,
) {
    companion object {
        const val KEY_ROTATE_LEFT = "rotor.rotateLeft"
        const val KEY_ROTATE_RIGHT = "rotor.rotateRight"
    }
}
