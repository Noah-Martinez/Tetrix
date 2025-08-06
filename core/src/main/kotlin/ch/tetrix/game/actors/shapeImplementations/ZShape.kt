package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.game.actors.TwoOrientationShape
import ch.tetrix.game.models.GridPosition
import ktx.inject.Context

class ZShape(
    context: Context,
    position: GridPosition?,
) : TwoOrientationShape(
    position ?: GridPosition(8, 0),
    arrayOf(
        GridPosition(-1, 0),
        GridPosition(0, 0),
        GridPosition(0, 1),
        GridPosition(1, 1),
    ),
    context,
) {
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
