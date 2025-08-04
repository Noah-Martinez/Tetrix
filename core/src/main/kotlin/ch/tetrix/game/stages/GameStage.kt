package ch.tetrix.game.stages

import ch.tetrix.game.Directions
import ch.tetrix.game.GridPosition
import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.CubeShape
import ch.tetrix.game.screens.ComponentBackground
import ch.tetrix.game.screens.GAME_VIEWPORT_HEIGHT
import ch.tetrix.game.screens.GAME_VIEWPORT_WIDTH
import ch.tetrix.shared.KeyHoldConfig
import ch.tetrix.shared.KeyHoldSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.max
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
): Stage(FitViewport(GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT)) {
    private val skin = context.inject<Skin>()
    private val inputMultiplexer = context.inject<InputMultiplexer>()
    private val componentBackground = context.inject<ComponentBackground>().drawable

    private val numCols: Int = 17
    private val numRows: Int = 36

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
            Pair(Input.Keys.E, KeyHoldConfig(0.2f) { activeShape?.rotateClockwise() }),
            Pair(Input.Keys.Q, KeyHoldConfig(0.2f) { activeShape?.rotateCounterClockwise() }),
            Pair(Input.Keys.W, KeyHoldConfig(0.2f) { activeShape?.move(Directions.UP) }),
            Pair(Input.Keys.S, KeyHoldConfig(0.2f) { activeShape?.move(Directions.DOWN) }),
            Pair(Input.Keys.A, KeyHoldConfig(0.2f) { activeShape?.move(Directions.LEFT) }),
            Pair(Input.Keys.D, KeyHoldConfig(0.2f) { activeShape?.move(Directions.RIGHT) }),
        )
    )
    var tableSizeSet = false

    val table = KTableWidget(skin).apply {
        setBackground(componentBackground)

        top()
        left()

        defaults().uniform()
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                add()
            }
            row()
        }
    }

    /** map of all cubes, to get if a position is occupied */
    val cubes = mutableMapOf<GridPosition, Cube>()

    /** actively controlled shape */
    private var activeShape: CubeShape? = null

    private val rotor: CubeShape = CubeShape(
        GridPosition(8, numRows - 8),
        arrayOf(
            GridPosition(0, 0)
        ),
        context,
    )

    init {
        context.register {
            context.bindSingleton<ModelBatch>(modelBatch)
            context.bindSingleton<Environment>(stageEnvironment)
        }

        rotateAndFitCamera(cameraRotationDeg)

        inputMultiplexer.addProcessor(keyHoldSystem)

        addActor(table)

        addShape(rotor)

        // TODO: remove debug shapes
        addShape(
            CubeShape(
                GridPosition(7, 9),
                arrayOf(
                    GridPosition(0, 0),
                    GridPosition(0, 1),
                    GridPosition(-1, 0),
                    GridPosition(1, 0),
                ),
                context,
            )
        )
        // TODO: see note:
        // NOTE: shape muss wahrscheinlich abstrakt gemacht werden. Da für den würfel zum Beispiel keine rotation möglich ist
        addShape(
            CubeShape(
                GridPosition(8, 12),
                arrayOf(
                    GridPosition(0, 0),
                    GridPosition(1, 0),
                    GridPosition(0, 1),
                    GridPosition(1, 1),
                ),
                context,
            )
        )
        addShape(
            CubeShape(
                GridPosition(5, 10),
                arrayOf(
                    GridPosition(0, 0),
                    GridPosition(0, 1),
                    GridPosition(-1, 0),
                    GridPosition(1, 0),
                ),
                context,
            )
        )
    }

    override fun act(delta: Float) {
        super.act(delta)
        activeShape?.act(delta)
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

    fun isOutOfBounds(pos: GridPosition): Boolean {
        return pos.x < 0 || pos.x >= numCols || pos.y < 0 || pos.y >= numRows
    }

    private fun addShape(cubeShape: CubeShape) {
        addActor(cubeShape)
        activeShape = cubeShape

        // register cubes of shape
        for (actor in cubeShape.children) {
            if (actor is Cube) {
                val pos = actor.gridPos
                if (!isOutOfBounds(pos)) {
                    cubes[pos] = actor
                }
            }
        }
    }

    /** gets the coordinates of a cell in the grid from a grid position */
    fun gridToWorldPos(pos: GridPosition): Vector2 {
        if (isOutOfBounds(pos)) {
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
        if (isOutOfBounds(pos)) {
            error("position out of bounds")
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
        activeShape = null
        cubes.values.forEach { cube -> cube.dispose() }
    }
}
