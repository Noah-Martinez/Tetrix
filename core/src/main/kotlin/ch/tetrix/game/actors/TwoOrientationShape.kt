package ch.tetrix.game.actors

import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.Orientation
import ch.tetrix.game.services.GameService
import com.badlogic.gdx.graphics.Texture
import ktx.inject.Context

/** Shape with only tow orientation states (Horizontal / Vertical) */
abstract class TwoOrientationShape(
    gridPos: GridPosition,
    cubePositions: Array<GridPosition>,
    context: Context,
    texture: Texture
) : Shape(gridPos, cubePositions, context, texture) {
    // NOTE: only has 2 rotation states not 4 like the default shape
    private var orientation = Orientation.Horizontal

    /** must have the same length as cubePositions */
    protected abstract val horizontalPositions: Array<GridPosition>
    /** must have the same length as cubePositions */
    protected abstract val verticalPositions: Array<GridPosition>

    override fun rotateClockwise(): Boolean {
        return rotate()
    }

    override fun rotateCounterClockwise(): Boolean {
        return rotate()
    }

    private fun rotate(): Boolean {
        when(orientation) {
            Orientation.Vertical -> {
                val couldRotate = doRotate(horizontalPositions)
                if (!couldRotate) {
                    return false
                }
                orientation = Orientation.Horizontal
            }
            Orientation.Horizontal -> {
                val couldRotate = doRotate(verticalPositions)
                if (!couldRotate) {
                    return false
                }
                orientation = Orientation.Vertical
            }
        }
        return true
    }

    private fun doRotate(endPositions: Array<GridPosition>): Boolean {
        val canRotate = endPositions.all {
            val endPos = gridPos + it
            val cubeAtPos = GameService.cubePositions[endPos]
            (cubeAtPos == null || cubeAtPos.shape == this) && !GameService.isOutOfBounds(endPos)
        }

        if (!canRotate) {
            log.info { "Unable to rotate active shape!" }
            return false
        }

        cubes.forEachIndexed { index, cube ->
            cube.localPos = endPositions[index]
        }
        return true
    }
}
