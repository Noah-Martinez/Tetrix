package ch.tetrix.game.models

import ch.tetrix.game.actors.Shape
import ch.tetrix.game.actors.shapeImplementations.*
import ktx.inject.Context

enum class ShapeType(private val factory: (Context, GridPosition?) -> Shape) {
    I (::IShape),
    J (::JShape),
    L (::LShape),
    O (::OShape),
    S (::SShape),
    T (::TShape),
    Z (::ZShape);

    fun create(context: Context, position: GridPosition? = null): Shape =
        factory(context, position)

    companion object {
        fun randomType(): ShapeType = entries.toTypedArray().random()
        fun randomShape(context: Context, position: GridPosition? = null): Shape =
            randomType().create(context, position)
    }
}
