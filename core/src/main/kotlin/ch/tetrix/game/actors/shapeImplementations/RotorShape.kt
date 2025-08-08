package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.game.actors.Shape
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import ktx.inject.Context

class RotorShape(
    context: Context,
) : Shape(
    GridPosition(8, 27),
    arrayOf(
        GridPosition(0, 0),
    ),
    context,
) {

    init {
        cubes[0].modelInstance.materials.first().set(ColorAttribute.createDiffuse(Color.RED))
    }

    override fun move(direction: Directions): MoveResult {
        return MoveResult.Success
    }
}
