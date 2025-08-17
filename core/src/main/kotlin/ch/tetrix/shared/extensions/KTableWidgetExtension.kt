package ch.tetrix.shared.extensions

import ch.tetrix.game.models.GridPosition
import com.badlogic.gdx.math.Vector2
import kotlin.math.max
import ktx.math.vec2
import ktx.scene2d.KTableWidget

/**
 * Resizes all cells in the KTableWidget to have a uniform size, ensuring consistent dimensions
 * across the table. The method computes a uniform cell size based on the table's dimensions,
 * then applies this size to all cells. It also adjusts the table's total width and height
 * accordingly and invalidates the layout hierarchy to reflect the changes.
 *
 * @return The computed uniform cell size applied to all cells in the table.
 */
fun KTableWidget.resizeToUniformCells(): Float {
    val cellSize = this.computeUniformCellSize()

    this.cells.forEach { cell ->
        cell.size(cellSize)
    }

    this.width = this.columns * cellSize
    this.height = this.rows * cellSize

    this.invalidateHierarchy()
    return cellSize
}

/**
 * Calculates a uniform cell size for the table, ensuring a consistent size
 * for all cells while maintaining the aspect ratio based on the table's dimensions.
 * The size is determined by the maximum value between the width and height
 * of an individual cell, derived from the available space.
 *
 * @return The computed uniform size for the cells in the table.
 * @throws IllegalArgumentException If the table has zero or negative rows or columns.
 */
fun KTableWidget.computeUniformCellSize(): Float {
    require(columns > 0 && rows > 0) { "Table must have positive rows and columns" }

    val bg = this.background
    val bgMinW = bg?.minWidth ?: 0f
    val bgMinH = bg?.minHeight ?: 0f

    val innerW = this.width - bgMinW
    val innerH = this.height - bgMinH

    val cellW = innerW / columns
    val cellH = innerH / rows

    return max(cellW, cellH)
}

/**
 * Converts a grid position within the table widget to a world position in stage coordinates.
 *
 * This method retrieves the position of the cell at the given grid coordinates,
 * then translates its local (table-specific) position to global stage coordinates.
 * This is useful for obtaining the exact position of an element in the table
 * relative to the stage, particularly when the table is nested within other containers.
 *
 * @param pos The grid position to convert, represented as a GridPosition object with x and y coordinates.
 * @return A Vector2 representing the world position of the specified grid cell in stage coordinates.
 */
fun KTableWidget.gridToWorldPos(pos: GridPosition): Vector2 {
    if (pos.x !in 0..columns || pos.y !in 0..rows) {
        error("Position $pos not in grid")
    }

    val cell = this.cells[(pos.y * this.columns) + pos.x]

    // Convert the cell's local (table) coordinates into stage coordinates.
    // Using table.x/y directly is wrong when the table is nested in other containers.
    val cellLocal = vec2(cell.actorX, cell.actorY)
    return this.localToStageCoordinates(cellLocal)
}

/**
 * Converts a grid cell position to its corresponding world scale dimensions.
 *
 * This method retrieves the associated cell in the table widget for the given grid position
 * and calculates a vector representing the width and height of the cell in world units.
 *
 * @param pos The grid position to convert, containing x and y coordinates.
 * @return A vector (Vector2) representing the world scale dimensions of the grid cell at the specified position.
 */
fun KTableWidget.gridToWorldScale(pos: GridPosition): Vector2 {
    if (pos.x !in 0..columns || pos.y !in 0..rows) {
        error("Position $pos not in grid")
    }

    val cell = this.cells[(pos.y * this.columns) + pos.x]
    return vec2(cell.actorWidth, cell.actorHeight)
}
