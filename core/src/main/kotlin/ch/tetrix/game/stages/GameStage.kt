package ch.tetrix.game.stages

import ch.tetrix.game.actors.Cube
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.screens.ComponentBackground
import ch.tetrix.game.screens.GAME_VIEWPORT_HEIGHT
import ch.tetrix.game.screens.GAME_VIEWPORT_WIDTH
import ch.tetrix.game.services.GameService
import ch.tetrix.shared.KeyHoldConfig
import ch.tetrix.shared.KeyHoldSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
import kotlin.math.max
import ktx.app.KtxInputAdapter
import ktx.inject.Context
import ktx.inject.register
import ktx.math.plus
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.KTableWidget
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx

/** visual table, also used to get the size for cubes */
class GameStage(
    val context: Context,
    batch: Batch,
): Stage(FitViewport(GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT), batch) {
    private val skin = context.inject<Skin>()
    private val inputMultiplexer = context.inject<InputMultiplexer>()
    private val componentBackground = context.inject<ComponentBackground>().drawable

    private val modelBatch = ModelBatch()

    /** changes the rotation angles for the camera, viewport and cube positions are adjusted to fit the gird */
    private val cameraRotationDeg = vec2(-3f, 2f)

    /** Rotation of the stage directional light. Will be normalized so keep between 1 and -1. */
    private val lightRotationNor = vec3(-1f, -1f, 0f)

    private val stageEnvironment = Environment().apply {
        add(DirectionalLightEx().apply {
            color.set(0.3f, 0.3f, 0.3f, 0.1f)
            direction.set(lightRotationNor).nor()
        })
        set(ColorAttribute.createAmbientLight(0.8f, 0.8f, 0.8f, 0.3f))
    }

    private val keyHoldSystem = KeyHoldSystem(
        mapOf(
            Pair(Input.Keys.A, KeyHoldConfig(0.2f) { handleKeyInputs(it) }),
            Pair(Input.Keys.D, KeyHoldConfig(0.2f) { handleKeyInputs(it) }),
    ))

    /**
     * Eigener InputAdapter für W/S Tasten, um fastFall zu steuern
     */
    private val movementInputAdapter = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            if (!GameService.isGameActive.value) {
                return false
            }

            when (keycode) {
                Input.Keys.W -> {
                    return GameService.enableFastFall(Directions.UP)
                }
                Input.Keys.S -> {
                    return GameService.enableFastFall(Directions.DOWN)
                }
                Input.Keys.Q -> {
                    GameService.rotateActiveShapeCounterClockwise()
                    return true
                }
                Input.Keys.E -> {
                    GameService.rotateActiveShapeClockwise()
                    return true
                }
                Input.Keys.J -> {
                    GameService.rotateRotorCounterClockwise()
                    return true
                }
                Input.Keys.L -> {
                    GameService.rotateRotorClockwise()
                    return true
                }
            }
            return false
        }

        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                Input.Keys.W, Input.Keys.S -> {
                    GameService.disableFastFall()
                    return true
                }
            }
            return false
        }
    }
    var tableSizeSet = false
    val table = KTableWidget(skin).apply {
        setBackground(componentBackground)

        top()
        left()

        defaults().uniform()
        repeat(GameService.NUM_ROWS) {
            repeat(GameService.NUM_COLS) {
                add()
            }
            row()
        }
    }

    init {
        context.register {
            context.bindSingleton<ModelBatch>(modelBatch)
            context.bindSingleton<Environment>(stageEnvironment)
        }

        rotateAndFitCamera(cameraRotationDeg)

        inputMultiplexer.addProcessor(keyHoldSystem)
        inputMultiplexer.addProcessor(movementInputAdapter)

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
        if (!GameService.isGameActive.value) {
            return
        }

        when (keycode) {
            Input.Keys.A -> {
                GameService.moveActiveShape(Directions.LEFT)
            }
            Input.Keys.D -> {
                GameService.moveActiveShape(Directions.RIGHT)
            }
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
        if (GameService.isOutOfBounds(pos)) {
            error("position $pos out of bounds")
        }
        val cell = table.cells[(pos.y * table.columns) + pos.x]

        val tablePos = vec2(table.x, table.y)
        val cellPos = vec2(cell.actorX, cell.actorY)
        // TODO-performance: store and update when needed instead of calculating every time
        val compensatedPos = getCameraAngleCompensation()

        return tablePos + cellPos + compensatedPos
    }

    private fun getCameraAngleCompensation(): Vector2 {
        val zFront = Cube.Companion.MODEL_DEPTH / 2f

        val radX = cameraRotationDeg.x * MathUtils.degRad
        val radY = cameraRotationDeg.y * MathUtils.degRad

        val dy = zFront * MathUtils.tan(radX)
        val dx = -zFront * MathUtils.tan(radY)

        return Vector2(dx, dy)
    }

    /** gets the dimensions of a cell in the grid from a grid position */
    fun gridToWorldScale(pos: GridPosition): Vector2 {
        if (GameService.isOutOfBounds(pos)) {
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
        inputMultiplexer.removeProcessor(movementInputAdapter)
        GameService.disposeAllCubes()
    }
}
