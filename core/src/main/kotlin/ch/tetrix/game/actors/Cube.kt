package ch.tetrix.game.actors

import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import ch.tetrix.game.services.GameService
import ch.tetrix.game.stages.GameStage
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.inject.Context
import ktx.log.logger
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

    private lateinit var grid: GameStage

    val shape: Shape
        get() = parent as Shape

    val gridPos: GridPosition
        get() = shape.gridPos + localPos

    companion object {
        const val MODEL_DEPTH: Float = 50f
        private val log = logger<Cube>()
    }

    // cube model
    private val modelAsset = Gdx.files.internal("model/cube.glb")
    private val model = GLBLoader().load(modelAsset)
    val modelInstance = ModelInstance(model.scene.model)

    override fun setStage(stage: Stage?) {
        super.setStage(stage)

        if (stage !is GameStage) {
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
    fun canMove(direction: Directions): MoveResult {
        val nextPos = gridPos + direction
        return checkCollision(nextPos)
    }

    fun canRotateClockwise(): Boolean {
        val nextLocalPos = localPos.rotatedClockwise()
        return when (checkCollision(nextLocalPos + shape.gridPos)) {
            is MoveResult.Collision -> false
            is MoveResult.Success -> true
        }
    }

    /** doesn't check validity/collisions of action */
    fun rotateClockwise() {
        localPos = localPos.rotatedClockwise()
    }

    fun canRotateCounterClockwise(): Boolean {
        val nextLocalPos = localPos.rotatedCounterClockwise()
        return when (checkCollision(nextLocalPos + shape.gridPos)) {
            is MoveResult.Collision -> false
            is MoveResult.Success -> true
        }
    }

    /** doesn't check validity/collisions of action */
    fun rotateCounterClockwise() {
        localPos = localPos.rotatedCounterClockwise()
    }

    /** @param gridPos must be a grid position, local positions will not work */
    private fun checkCollision(gridPos: GridPosition): MoveResult {
        if (GameService.isOutOfBounds(gridPos)) {
            return MoveResult.Collision(null)
        }

        val nextPosCube = GameService.cubePositions[gridPos]
        if (nextPosCube != null && nextPosCube !in shape.cubes) {
            return MoveResult.Collision(nextPosCube.shape)
        }
        return MoveResult.Success
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
