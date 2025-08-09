package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.assets.TextureAssets
import ch.tetrix.assets.get
import ch.tetrix.game.actors.Shape
import ch.tetrix.game.models.GridPosition
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context

class TShape(
    context: Context,
    position: GridPosition?,
) : Shape(
    position ?: GridPosition(8, 0),
    arrayOf(
        GridPosition(-1, 0),
        GridPosition(0, 0),
        GridPosition(1, 0),
        GridPosition(0, 1),
    ),
    context,
    texture = context.inject<AssetManager>()[TextureAssets.CUBE_PURPLE]
)
