package ch.tetrix.game

import ch.tetrix.game.ui.GameField
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.inject.Context
import ktx.inject.register
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.Scene2DSkin
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : KtxScreen {
    private lateinit var modelBatch: ModelBatch
    private lateinit var stage: Stage
    private lateinit var inputMultiplexer: InputMultiplexer
    private lateinit var gameField: GameField

    /** changes the rotation angles for the camera, viewport and cube positions are adjusted to fit the gird */
    private val cameraRotationDeg = vec2(-0.5f, 0.2f)

    /** Rotation of the stage directional light. Will be normalized so keep between 1 and -1. */
    private val lightRotationNor = vec3(-1f, -1f, 0f)

    private val nCols: Int = 17
    private val nRows: Int = 36

    override fun show() {
        super.show()
        inputMultiplexer = context.inject()

        stage = Stage(FitViewport(nCols.toFloat(), nRows.toFloat()))
        inputMultiplexer.addProcessor(stage)
        modelBatch = ModelBatch()

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
        rotateAndFitCamera(cameraRotationDeg)

        gameField = GameField(nCols, nRows, cameraRotationDeg, context)
        stage.addActor(gameField)

        stage.act()
        stage.draw()
    }

    private fun rotateAndFitCamera(anglesDeg: Vector2) {
        stage.camera.apply {
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
            if (anglesDeg.x != 0f) viewportHeight = nRows / MathUtils.cos(radX)
            if (anglesDeg.y != 0f) viewportWidth = nCols / MathUtils.cos(radY)

            update()
        }
    }

    override fun render(delta: Float) {
        val backgroundColor = Scene2DSkin.defaultSkin.getColor("primary")
        clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glDepthFunc(GL20.GL_LESS)
        Gdx.gl.glEnable(GL20.GL_CULL_FACE)
        Gdx.gl.glCullFace(GL20.GL_BACK)

        modelBatch.begin(stage.camera)
        stage.act(delta)
        stage.draw()
        modelBatch.end()
    }


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        if (::stage.isInitialized) {
            stage.dispose()
        }

        if (::gameField.isInitialized) {
            gameField.dispose()
        }

        inputMultiplexer.removeProcessor(stage)
    }
}
