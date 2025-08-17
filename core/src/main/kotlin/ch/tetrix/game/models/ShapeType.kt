package ch.tetrix.game.models

import ch.tetrix.game.actors.Shape
import ch.tetrix.game.actors.shapeImplementations.IShape
import ch.tetrix.game.actors.shapeImplementations.JShape
import ch.tetrix.game.actors.shapeImplementations.LShape
import ch.tetrix.game.actors.shapeImplementations.OShape
import ch.tetrix.game.actors.shapeImplementations.SShape
import ch.tetrix.game.actors.shapeImplementations.TShape
import ch.tetrix.game.actors.shapeImplementations.ZShape
import java.util.EnumMap
import ktx.inject.Context
import ktx.scene2d.KTableWidget

enum class ShapeType(private val factory: (KTableWidget, Context, GridPosition?) -> Shape) {
    I (::IShape),
    J (::JShape),
    L (::LShape),
    O (::OShape),
    S (::SShape),
    T (::TShape),
    Z (::ZShape);

    companion object {
        private const val WINDOW = 12 // Jedes shape erscheint mindestens ein mal alle X shapes
        private const val BOOST_START = 3 // Erhöhe Wahrscheinlichkeit nach dem X-ten mal wo dieses shape nicht auftritt
        private const val BOOST_FACTOR = 0.25
        private const val REPEAT_PENALTY = 0.6 // Penalty für wiederholen der gleichen shape

        private var rng: kotlin.random.Random = kotlin.random.Random.Default
        private val drought = EnumMap<ShapeType, Int>(ShapeType::class.java).apply {
            entries.forEach { this[it.key] = 0 }
        }
        private var last: ShapeType? = null
        private var last2: ShapeType? = null

        /**
         * Randomly selects a ShapeType based on weighted probabilities derived from various conditions.
         *
         * The method considers factors such as drought (how long a shape type has not been picked),
         * repeat penalties for the last selected shape type, and a boosting mechanism for types that
         * have been absent for a longer duration. It ensures balanced randomness with custom rules
         * to influence shape selection in gameplay.
         *
         * @return A randomly selected ShapeType that adheres to the weighted randomness criteria.
         */
        fun randomType(): ShapeType {
            val (maxType, maxDrought) = entries
                .maxBy { drought[it] ?: 0 }
                .let { it to (drought[it] ?: 0) }

            if (maxDrought >= WINDOW - 1) {
                return finalizePick(maxType)
            }

            val weights = entries.associateWith { type ->
                val drought = drought[type] ?: 0
                var weight = 1.0 + maxOf(0, drought - BOOST_START) * BOOST_FACTOR
                if (type == last) weight *= REPEAT_PENALTY
                weight
            }

            val total = weights.values.sum()
            val pick = if (total <= 0.0) {
                entries.random(rng)
            } else {
                val r = rng.nextDouble(total)
                var acc = 0.0
                var chosen = entries.first()
                for ((t, w) in weights) {
                    acc += w
                    if (r <= acc) { chosen = t; break }
                }
                chosen
            }

            return finalizePick(pick)
        }

        private fun finalizePick(pick: ShapeType): ShapeType {
            entries.forEach { drought[it] = (drought[it] ?: 0) + 1 }
            drought[pick] = 0
            last2 = last
            last = pick
            return pick
        }
    }

    /**
     * Creates a new Shape instance using the provided parameters.
     *
     * @param gridTable The grid table where the shape will be placed.
     *                  It provides the context of the grid layout and associated functionality.
     * @param context The execution context that contains game-related resources and logic.
     * @param position The optional initial grid position where the shape will be placed.
     *                 If null, a default position may be used by the factory function.
     * @return A newly created Shape instance initialized with the provided parameters.
     */
    fun create(gridTable: KTableWidget, context: Context, position: GridPosition? = null): Shape =
        factory(gridTable, context, position)
}
