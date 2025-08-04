package ch.tetrix.game.actors

import ch.tetrix.game.Directions
import ch.tetrix.game.GridPosition
import ch.tetrix.game.stages.GameStage
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.inject.Context

/**
 * CubeShape is a Group of multiple Cube actors.
 * It has a direction which can be changed.
 */
class CubeShape(
    /** position on grid also rotation anchor */
    gridPos: GridPosition,
    /** positions of children relative to shape grid position */
    val cubePositions: Array<GridPosition>,
    val context: Context,
): Group() {
    private val _cubes = arrayListOf<Cube>()
    val cubes: Array<Cube>
        get() = _cubes.toTypedArray()

    private var _gridPos = gridPos
    val gridPos: GridPosition
        get() = _gridPos

    private lateinit var grid: GameStage

    init {
        isTransform = true
    }

    /** actual init of shape on grid */
    override fun setStage(stage: Stage?) {
        super.setStage(stage)

        if (stage !is GameStage) {
            return
        }

        grid = stage

        cubePositions.forEach {
            val cubeGridPos = it + gridPos
            if (grid.cubes[cubeGridPos] != null || grid.isOutOfBounds(cubeGridPos)) {
                // TODO: do something else than throw error
                error("Cube cannot be initialized in occupied or out of bounds position: $cubeGridPos")
            }

            val cube = Cube(it, context)
            addActor(cube)
            _cubes.add(cube)
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        children.forEach { it.act(delta) }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!isVisible) return
        super.draw(batch, parentAlpha)
        super.drawChildren(batch, parentAlpha)
    }

    fun move(direction: Directions) {
        val canMove = cubes.all { it.canMove(direction) }
        if (!canMove) {
            println("Unable to move active shape ${direction.name}!") // TODO: remove console log
            return // TODO: add visual feedback of impossible move
        }

        _gridPos += direction
    }

    fun rotateClockwise() {
        val canRotate = cubes.all { it.canRotateClockwise() }
        if (!canRotate) {
            println("Unable to rotate active shape clockwise!") // TODO: remove console log
            return // TODO: add visual feedback of impossible move
        }

        cubes.forEach {
            it.rotateClockwise()
        }
    }

    fun rotateCounterClockwise() {
        val canRotate = cubes.all { it.canRotateCounterClockwise() }
        if (!canRotate) {
            println("Unable to rotate active shape counterclockwise!") // TODO: remove console log
            return // TODO: add visual feedback of impossible move
        }

        cubes.forEach {
            it.rotateCounterClockwise()
        }
    }
}
