package ch.tetrix.game.actors

import ch.tetrix.game.Directions
import ch.tetrix.game.GridPosition
import ch.tetrix.game.components.GameComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.inject.Context
import ktx.math.vec3
import net.mgsx.gltf.loaders.glb.GLBLoader

// NOTE: No move function necessary, since gridPosition is based on parent position, so we only need a check for it.
class Cube(
    /** position relative to parent */
    var localPos: GridPosition,
    var context: Context,
): Actor() {
    private val modelBatch: ModelBatch = context.inject()
    private val environment: Environment = context.inject()

    private lateinit var grid: GameComponent

    val shape by lazy { parent as CubeShape }

    val gridPos: GridPosition
        get() = shape.gridPos + localPos

    companion object {
        const val MODEL_DEPTH: Float = 50f
    }

    // cube model
    private val modelAsset = Gdx.files.internal("model/cube.glb")
    private val model = GLBLoader().load(modelAsset)
    private val modelInstance = ModelInstance(model.scene.model)

    override fun setStage(stage: Stage?) {
        super.setStage(stage)

        if (stage !is GameComponent) {
            return
        }

        grid = stage
    }

    /** makes sure the model is correctly positioned and sized for the grid's cell it belongs to */
    override fun act(delta: Float) {
        super.act(delta)
        val cellDimension = grid.gridToWorldScale(gridPos)
        width = cellDimension.x
        height = cellDimension.y

        val worldPos = grid.gridToWorldPos(gridPos)
        setPosition(worldPos.x, worldPos.y)
    }

    /* NOTE:
     * Check funktionen müssen unabhängig von den tatsächlichen Aktionen aufrufbar sein.
     * Da ansonsten partial moves/rotates vorkommen könnten, bei denen nur ein Paar Cubes einer Shape ge moved/rotiert werden.
     */
    fun canMove(direction: Directions): Boolean {
        val nextPos = gridPos + direction
        return checkCollision(nextPos)
    }

    fun canRotateClockwise(): Boolean {
        val nextLocalPos = localPos.rotatedClockwise()
        return checkCollision(nextLocalPos + shape.gridPos)
    }

    /** doesn't check validity/collisions of action */
    fun rotateClockwise() {
        localPos = localPos.rotatedClockwise()
    }

    fun canRotateCounterClockwise(): Boolean {
        val nextLocalPos = localPos.rotatedCounterClockwise()
        return checkCollision(nextLocalPos + shape.gridPos)
    }

    /** doesn't check validity/collisions of action */
    fun rotateCounterClockwise() {
        localPos = localPos.rotatedCounterClockwise()
    }

    /** @param gridPos must be a grid position, local positions will not work */
    private fun checkCollision(gridPos: GridPosition): Boolean {
        if (grid.isOutOfBounds(gridPos)) {
            return false
        }

        val nextPosCube = grid.cubes[gridPos]
        return !(nextPosCube != null && nextPosCube !in shape.cubes)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // NOTE: z = -MODEL_DEPTH wenn das front-face des cube mit dem grid aligned sein soll
        val worldPos = vec3(x + width/2f, y + height/2f, 0f)
        val scaleVec = vec3(width, height, MODEL_DEPTH)

        modelInstance.transform.idt()
            .setToTranslation(worldPos)
            .scl(scaleVec)

        modelBatch.render(modelInstance, environment)
    }

    fun dispose() {
        model.dispose()
    }
}
