package ch.tetrix.game.models

import ch.tetrix.game.actors.Shape

sealed class MoveResult {
    /** moved successfully without obstructions */
    object Success : MoveResult()

    /** @param collisionObject either a shape or null (the game borders) */
    data class Collision(val collisionObject: Shape?) : MoveResult()
}
