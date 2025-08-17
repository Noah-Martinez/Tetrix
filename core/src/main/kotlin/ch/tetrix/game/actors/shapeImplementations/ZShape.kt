package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.assets.TextureAssets
import ch.tetrix.assets.get
import ch.tetrix.game.actors.TwoOrientationShape
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.ShapeType
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context
import ktx.scene2d.KTableWidget

class ZShape(
    gridTable: KTableWidget,
    context: Context,
    position: GridPosition?,
) : TwoOrientationShape(
    gridTable,
    position ?: GridPosition(8, 0),
    arrayOf(
        GridPosition(-1, 0),
        GridPosition(0, 0),
        GridPosition(0, 1),
        GridPosition(1, 1),
    ),
    context,
    texture = context.inject<AssetManager>()[TextureAssets.CUBE_MAGENTA],
) {
    override val shapeType = ShapeType.Z

    override val horizontalPositions = arrayOf(
        GridPosition(-1, 0),
        GridPosition(0, 0),
        GridPosition(0, 1),
        GridPosition(1, 1),
    )

    override val verticalPositions = arrayOf(
        GridPosition(0, -1),
        GridPosition(0, 0),
        GridPosition(-1, 0),
        GridPosition(-1, 1),
    )
}
