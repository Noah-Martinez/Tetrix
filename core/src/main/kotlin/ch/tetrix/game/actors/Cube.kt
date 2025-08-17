package ch.tetrix.game.actors

import ch.tetrix.assets.ModelAssets
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import ch.tetrix.game.services.GameService
import ch.tetrix.shared.extensions.gridToWorldPos
import ch.tetrix.shared.extensions.gridToWorldScale
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.inject.Context
import ktx.log.logger
import ktx.math.vec3
import ktx.scene2d.KTableWidget

class Cube(
    private val gridTable: KTableWidget,
    var localPos: GridPosition,
    var shape: Shape,
    context: Context,
    private val texture: Texture,
    private val glow: Boolean = false,
): Actor() {
    private val gameService : GameService = context.inject()
    private val modelBatch: ModelBatch = context.inject()
    private val environment: Environment = context.inject()

    val gridPos: GridPosition
        get() = shape.gridPos + localPos

    val modelInstance: ModelInstance = ModelAssets.Cube.createInstance()

    companion object {
        const val MODEL_DEPTH: Float = 50f
        private val log = logger<Cube>()
    }

    init {
        modelInstance.materials.forEach { mat ->
            mat.set(TextureAttribute.createDiffuse(texture))
            if (glow) {
                mat.set(TextureAttribute.createEmissive(texture))
            }
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        val cellDimension = gridTable.gridToWorldScale(gridPos)
        width = kotlin.math.round(cellDimension.x)
        height = kotlin.math.round(cellDimension.y)

        val worldPos = gridTable.gridToWorldPos(gridPos)
        setPosition(worldPos.x, worldPos.y)
    }

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

    fun rotateCounterClockwise() {
        localPos = localPos.rotatedCounterClockwise()
    }

    private fun checkCollision(gridPos: GridPosition): MoveResult {
        if (gameService.isOutOfBounds(gridPos)) {
            return MoveResult.Collision(null)
        }

        val nextPosCube = gameService.cubePositions[gridPos]
        if (nextPosCube != null && nextPosCube !in shape.cubes) {
            return MoveResult.Collision(nextPosCube.shape)
        }
        return MoveResult.Success
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // NOTE: z = -MODEL_DEPTH wenn das front-face des cube mit dem grid aligned sein soll
        val worldPos = vec3(x + width/2f, y + height/2f, 0f)

        modelInstance.transform
            .idt()
            .setToTranslation(worldPos)
            .scl(width, height, MODEL_DEPTH)

        modelBatch.render(modelInstance, environment)
    }
}
