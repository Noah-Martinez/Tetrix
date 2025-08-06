package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.game.actors.Shape
import ch.tetrix.game.models.GridPosition
import ktx.inject.Context

class JShape(
    context: Context,
    position: GridPosition?,
) : Shape(
    position ?: GridPosition(8, 0),
    arrayOf(
        GridPosition(-1, 0),
        GridPosition(0, 0),
        GridPosition(1, 0),
        GridPosition(1, 1),
    ),
    context,
)
