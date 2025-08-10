package ch.tetrix.game.actors

import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Group
import ktx.inject.Context
import ktx.log.logger

/**
 * Shape is a Group of multiple Cube actors.
 * Helps with moving and rotating the cubes based on their relative position to the shape.
 */
abstract class Shape(
    /** position on grid also rotation anchor */
    gridPos: GridPosition,
    /** positions of children relative to shape grid position */
    cubePositions: Array<GridPosition>,
    val context: Context,
    private val texture: Texture
): Group() {
    protected val _cubes = arrayListOf<Cube>()
    val cubes: Array<Cube>
        get() = _cubes.toTypedArray()

    private var _gridPos = gridPos
    val gridPos: GridPosition
        get() = _gridPos

    companion object {
        @JvmStatic
        protected val log = logger<Shape>()
    }

    var fallDirection: Directions = Directions.DOWN

    init {
        isTransform = true

        cubePositions.forEach {
            val cube = Cube(it, this, context, texture)
            addActor(cube)
            _cubes.add(cube)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!isVisible) return
        super.draw(batch, parentAlpha)
        super.drawChildren(batch, parentAlpha)
    }

    open fun move(direction: Directions): MoveResult {
        if (direction == fallDirection.opposite()) {
            // ignore up/down moves when falling in the other direction
            return MoveResult.Success
        }

        val moveResults = cubes.map { it.canMove(direction) }
        val collided = moveResults.any { it is MoveResult.Collision }
        if (collided) {
            log.info { "Unable to move shape ${direction.name}!" }
            // shape collision has priority over border collision when multiple collisions occur
            val shapeCollision = moveResults.find { it is MoveResult.Collision && it.collisionObject is Shape }
            return shapeCollision ?: MoveResult.Collision(null)
        }

        _gridPos += direction
        return MoveResult.Success
    }

    open fun rotateClockwise(): Boolean {
        val canRotate = cubes.all { it.canRotateClockwise() }
        if (!canRotate) {
            log.info { "Unable to rotate shape clockwise!" }
            return false // TODO: add visual feedback of impossible move
        }

        cubes.forEach {
            it.rotateClockwise()
        }
        return true
    }

    open fun rotateCounterClockwise(): Boolean {
        val canRotate = cubes.all { it.canRotateCounterClockwise() }
        if (!canRotate) {
            log.info { "Unable to rotate shape counterclockwise!" }
            return false // TODO: add visual feedback of impossible move
        }

        cubes.forEach {
            it.rotateCounterClockwise()
        }
        return true
    }

    /**
     * Transfer cubes from shape to `targetShape`.
     * Grid positions of the cubes don't change, only their parent and local position relative to the new parent.
     *
     * @param targetShape The Shape instance to which the cubes will be transferred.
     * @return True if cubes were transferred, false if this shape had no cubes.
     */
    fun transferCubesTo(targetShape: Shape): Boolean {
        if (_cubes.isEmpty()) {
            log.info { "No cubes to transfer from shape (${this.javaClass.simpleName})." }
            return false
        }

        val cubesToTransfer = ArrayList(_cubes)

        _cubes.clear()

        cubesToTransfer.forEach { cube ->
            cube.localPos = cube.gridPos - targetShape.gridPos
            cube.shape = targetShape

            this.removeActor(cube)
            targetShape.addActor(cube)
            targetShape._cubes.add(cube)
            log.debug { "Transferred cube at absolute ${cube.gridPos} to target shape (${targetShape.javaClass.simpleName}) with new localPos: ${cube.localPos}" }
        }

        log.info { "Successfully transferred ${cubesToTransfer.size} cubes from shape (${this.javaClass.simpleName}) to shape (${targetShape.javaClass.simpleName})." }
        return true
    }
}
