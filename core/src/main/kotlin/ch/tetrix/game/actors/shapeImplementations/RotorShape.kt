package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.assets.TextureAssets
import ch.tetrix.assets.get
import ch.tetrix.game.actors.Shape
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context

class RotorShape(
    context: Context,
) : Shape(
    GridPosition(8, 27),
    arrayOf(
        GridPosition(0, 0),
    ),
    context,
    texture = context.inject<AssetManager>()[TextureAssets.CUBE_RED]
) {
    override fun move(direction: Directions): MoveResult {
        return MoveResult.Success
    }
}
