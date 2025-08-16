package ch.tetrix.game.stages

import ch.tetrix.GAME_HEIGHT
import ch.tetrix.GAME_WIDTH
import ch.tetrix.game.GameInputController
import ch.tetrix.game.actors.Cube.Companion.MODEL_DEPTH
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.services.GameService
import ch.tetrix.shared.ConfigManager
import ch.tetrix.shared.KeyHoldConfig
import ch.tetrix.shared.KeyHoldSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger
import ktx.math.plus
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.KTableWidget
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import kotlin.math.max

class GameStage(
    val context: Context,
    batch: Batch,
) : Stage(FitViewport(GAME_WIDTH, GAME_HEIGHT), batch) {
    private val gameService by lazy { context.inject<GameService>() }
    private val skin by lazy { context.inject<Skin>() }
    private val inputMultiplexer by lazy { context.inject<InputMultiplexer>() }

    private val config by lazy { ConfigManager.playerConfig }

    private val modelBatch = ModelBatch()

    private val cameraRotationDeg = vec2(-3f, 2f)
    private val lightRotationNor = vec3(-1f, -1f, 0f)

    private val cameraCompensation by lazy {
        val zFront = MODEL_DEPTH / 2f
        val radX = cameraRotationDeg.x * MathUtils.degRad
        val radY = cameraRotationDeg.y * MathUtils.degRad

        val dy = zFront * MathUtils.tan(radX)
        val dx = -zFront * MathUtils.tan(radY)

        Vector2(dx, dy)
    }

    private val stageEnvironment = Environment().apply {
        add(DirectionalLightEx().apply {
            color.set(0.3f, 0.3f, 0.3f, 0.1f)
            direction.set(lightRotationNor).nor()
        })
        set(ColorAttribute.createAmbientLight(0.8f, 0.8f, 0.8f, 0.3f))
    }

    private var keyHoldSystem: KeyHoldSystem = buildKeyHoldSystem()
    private fun buildKeyHoldSystem(): KeyHoldSystem =
        KeyHoldSystem(
            mapOf(
                config.tetromino.moveLeft to KeyHoldConfig(0.2f) { handleKeyInputs(it) },
                config.tetromino.moveRight to KeyHoldConfig(0.2f) { handleKeyInputs(it) },
            )
        )

    private val inputController = GameInputController(config) { action ->
        if (!gameService.isGameActive.value) return@GameInputController
        when (action) {
            GameInputController.Action.MoveUp -> gameService.enableFastFall(Directions.UP)
            GameInputController.Action.MoveDown -> gameService.enableFastFall(Directions.DOWN)
            GameInputController.Action.Snap -> gameService.snapActiveShape()
            GameInputController.Action.RotLeft -> gameService.rotateActiveShapeCounterClockwise()
            GameInputController.Action.RotRight -> gameService.rotateActiveShapeClockwise()
            GameInputController.Action.RotorLeft -> gameService.rotateRotorCounterClockwise()
            GameInputController.Action.RotorRight -> gameService.rotateRotorClockwise()
            GameInputController.Action.FastFallOff -> gameService.disableFastFall()
        }
    }

    companion object {
        private val log = logger<GameStage>()
    }

    var tableSizeSet = false
    val table = KTableWidget(skin).apply {

        val background = skin.getDrawable("game-background")
        setBackground(background)
        top()
        left()

        defaults().uniform()
        repeat(gameService.rows) {
            repeat(gameService.cols) { add() }
            row()
            // TODO: show max rotor size
        }
    }

    init {
        context.register {
            context.bindSingleton<ModelBatch>(modelBatch)
            context.bindSingleton<Environment>(stageEnvironment)
        }

        rotateAndFitCamera(cameraRotationDeg)

        inputMultiplexer.addProcessor(keyHoldSystem)
        inputMultiplexer.addProcessor(inputController)

        addActor(table)
    }

    override fun draw() {
        if (!tableSizeSet && table.width > 0) {
            setUniformTableSize()
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glDepthFunc(GL20.GL_LESS)
        Gdx.gl.glEnable(GL20.GL_CULL_FACE)
        Gdx.gl.glCullFace(GL20.GL_BACK)

        modelBatch.begin(viewport.camera)
        super.draw()
        modelBatch.end()

        Gdx.gl.glDisable(GL20.GL_CULL_FACE)
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
    }

    private fun handleKeyInputs(keycode: Int) {
        if (!gameService.isGameActive.value) return
        when (keycode) {
            config.tetromino.moveLeft -> gameService.moveActiveShape(Directions.LEFT)
            config.tetromino.moveRight -> gameService.moveActiveShape(Directions.RIGHT)
        }
    }

    private fun rotateAndFitCamera(anglesDeg: Vector2) {
        camera.apply {
            /*
             * NOTE: moved near-pane to negative space since Stage is on z:0 and can't be moved,
             * but when the camera is angled some items may end up in <0 and clipped otherwise.
             * That's why OrthographicCamera is usually only rotated around the z axis.
             */
            near = -100f

            if (anglesDeg.x != 0f) rotate(anglesDeg.x, 1f, 0f, 0f)
            if (anglesDeg.y != 0f) rotate(anglesDeg.y, 0f, 1f, 0f)

            val radX = anglesDeg.x * MathUtils.degreesToRadians
            val radY = anglesDeg.y * MathUtils.degreesToRadians
            if (anglesDeg.x != 0f) viewportHeight = this@GameStage.height / MathUtils.cos(radX)
            if (anglesDeg.y != 0f) viewportWidth = this@GameStage.width / MathUtils.cos(radY)

            update()
        }
    }

    private fun setUniformTableSize() {
        table.apply {
            val cellSize = max(
                (width - padX - background.minWidth) / columns,
                (height - padY - background.minHeight) / rows,
            )

            cells.forEach { cell -> cell.size(cellSize) }

            width = columns * cellSize
            height = rows * cellSize
        }
        tableSizeSet = true

        table.invalidateHierarchy()
    }

    /** gets the coordinates of a cell in the grid from a grid position */
    fun gridToWorldPos(pos: GridPosition): Vector2 {
        if (gameService.isOutOfBounds(pos)) {
            error("position $pos out of bounds")
        }
        val cell = table.cells[(pos.y * table.columns) + pos.x]

        val tablePos = vec2(table.x, table.y)
        val cellPos = vec2(cell.actorX, cell.actorY)

        return tablePos + cellPos + cameraCompensation
    }

    /** gets the dimensions of a cell in the grid from a grid position */
    fun gridToWorldScale(pos: GridPosition): Vector2 {
        if (gameService.isOutOfBounds(pos)) {
            error("position $pos out of bounds")
        }
        val cell = table.cells[(pos.y * table.columns) + pos.x]
        return vec2(cell.actorWidth, cell.actorHeight)
    }

    override fun dispose() {
        super.dispose()
        context.apply {
            remove<ModelBatch>()
            remove<Environment>()
        }
        inputMultiplexer.removeProcessor(keyHoldSystem)
        inputMultiplexer.removeProcessor(inputController)
    }
}
