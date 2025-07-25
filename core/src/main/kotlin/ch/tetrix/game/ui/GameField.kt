package ch.tetrix.game.ui

import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.CubeShape
import ch.tetrix.game.actors.Directions
import ch.tetrix.game.actors.GridPosition
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxInputAdapter
import ktx.inject.Context
import ktx.math.plus
import ktx.math.vec2

/** visual table, also used to get the size for cubes */
class GameField(
    val numCols: Int = 17,
    val numRows: Int = 36,
    val cameraRotationDeg: Vector2,
    val context: Context,
): Table(), Disposable, KtxInputAdapter {
    private val inputMultiplexer = context.inject<InputMultiplexer>()

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
        inputMultiplexer.addProcessor(this)

        debugCell() // TODO: remove debug
        top()
        left()
        setFillParent(true)

        buildTable()
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

    private fun buildTable() {
        clear()

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                add()
//                    (debug) show coordinates in cells:
//                    .add(Label("$r;$c", assets[SkinAssets.Default]))
                    .expand()
                    .fill()
                    .uniform()
            }
            row()
        }
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
        val cell = cells[(pos.y * columns) + pos.x]

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
        val cell = cells[(pos.y * columns) + pos.x]
        return vec2(cell.actorWidth, cell.actorHeight)
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(this)
        activeShape = null
        cubes.values.forEach { cube -> cube.dispose() }
    }
}
