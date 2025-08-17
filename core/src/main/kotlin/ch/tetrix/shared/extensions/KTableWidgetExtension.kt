package ch.tetrix.shared.extensions

import ch.tetrix.game.models.GridPosition
import com.badlogic.gdx.math.Vector2
import kotlin.math.max
import ktx.math.vec2
import ktx.scene2d.KTableWidget

/**
 * Extension Function für KTableWidget, die alle Zellen automatisch auf eine
 * einheitliche, quadratische Größe (1:1 Seitenverhältnis) anpasst.
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

fun KTableWidget.gridToWorldPos(pos: GridPosition): Vector2 {
    val cell = this.cells[(pos.y * this.columns) + pos.x]

    // Convert the cell's local (table) coordinates into stage coordinates.
    // Using table.x/y directly is wrong when the table is nested in other containers.
    val cellLocal = vec2(cell.actorX, cell.actorY)
    return this.localToStageCoordinates(cellLocal)
}

fun KTableWidget.gridToWorldScale(pos: GridPosition): Vector2 {
    val cell = this.cells[(pos.y * this.columns) + pos.x]
    return vec2(cell.actorWidth, cell.actorHeight)
}
