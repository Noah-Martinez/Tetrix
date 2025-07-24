package ch.tetrix.game.stages

import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.CubeShape
import ch.tetrix.game.actors.Directions
import ch.tetrix.game.actors.GridPosition
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.inject.register
import ktx.math.plus
import ktx.math.vec2
import ktx.math.vec3
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx

class GameField(
    /** number of columns of the game field */
    val width: Int = 17,
    /** number of rows of the game field */
    val height: Int = 36,
    val context: Context,
): Stage(FitViewport(width.toFloat(), height.toFloat())) {
    private val modelBatch = ModelBatch()
    private val inputMultiplexer = context.inject<InputMultiplexer>()

    /** changes the rotation angles for the camera, viewport and cube positions are adjusted to fit the gird */
    private val cameraRotationDeg = vec2(-0.5f, 0.2f)

    /** Rotation of the stage directional light. Will be normalized so keep between 1 and -1. */
    private val lightRotationNor = vec3(-1f, -1f, 0f)

    /** visual table, also used to get the size for cubes */
    private val table = Table().apply {
        debugCell() // TODO: remove debug
        top()
        left()
        setFillParent(true)
    }

    /** map of all cubes, to get if a position is occupied */
    val cubes = mutableMapOf<GridPosition, Cube>()

    /** actively controlled shape */
    private var activeShape: CubeShape? = null

    private val rotor: CubeShape = CubeShape(
        GridPosition(8, height - 8),
        arrayOf(
            GridPosition(0, 0)
        ),
        context,
    )

    init {
        context.register {
            bindSingleton<ModelBatch>(modelBatch)

            val stageEnvironment = Environment().apply {
                add(DirectionalLightEx().apply {
//                    isDebugAll = true
                    color.set(0.3f, 0.3f, 0.3f, 0.1f)
                    direction.set(lightRotationNor).nor()
                })
                set(ColorAttribute.createAmbientLight(0.8f, 0.8f, 0.8f, 0.3f))
            }
            bindSingleton<Environment>(stageEnvironment)
        }

        addActor(table)
        buildTable()
        addShape(rotor)

        rotateAndFitCamera(cameraRotationDeg)

        inputMultiplexer.addProcessor(this)

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

    private fun buildTable() {
        table.clear()

        for (r in 0 until height) {
            for (c in 0 until width) {
                table
                    .add()
//                    .add(Label("$r;$c", assets[SkinAssets.Default]))
                    .expand()
                    .fill()
                    .uniform()
            }
            table.row()
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
            if (anglesDeg.x != 0f) viewportHeight = this@GameField.height / MathUtils.cos(radX)
            if (anglesDeg.y != 0f) viewportWidth = this@GameField.width / MathUtils.cos(radY)

            update()
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        activeShape?.act(delta)
    }

    override fun draw() {
        modelBatch.begin(viewport.camera)
        super.draw()
        modelBatch.end()
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.E -> {
                activeShape?.rotateClockwise()
            }
            Input.Keys.Q -> {
                activeShape?.rotateCounterClockwise()
            }
            Input.Keys.W -> {
                activeShape?.move(Directions.UP)
            }
            Input.Keys.S -> {
                activeShape?.move(Directions.DOWN)
            }
            Input.Keys.A -> {
                activeShape?.move(Directions.LEFT)
            }
            Input.Keys.D -> {
                activeShape?.move(Directions.RIGHT)
            }
        }
        return true
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    fun isOutOfBounds(pos: GridPosition): Boolean {
        return pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height
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

        val actualPos = vec2(cell.actorX, cell.actorY)
        // TODO-performance: store and update when needed instead of calculating every time
        val compensatedPos = getCameraAngleCompensation()

        return actualPos + compensatedPos
    }

    private fun getCameraAngleCompensation(): Vector2 {
        val zFront = Cube.MODEL_DEPTH / 2f

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
        inputMultiplexer.removeProcessor(this)
        activeShape = null
        cubes.values.forEach { cube -> cube.dispose() }
    }
}
