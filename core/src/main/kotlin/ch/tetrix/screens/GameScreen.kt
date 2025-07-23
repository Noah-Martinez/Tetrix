package ch.tetrix.screens

import ch.tetrix.game.CubeManager
import ch.tetrix.game.EnvironmentManager
import ch.tetrix.ui.UIManager
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxScreen

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(
    private val batch: Batch,
    private val font: BitmapFont,
    private val assets: AssetManager,
    private var camera: OrthographicCamera,
    private val engine: PooledEngine
) : KtxScreen {
    private val cubeManager = CubeManager()
    private val environmentManager = EnvironmentManager(camera)
    private val uiManager = UIManager(cubeManager, assets)
    private val shapeRenderer = ShapeRenderer()

    init {
        cubeManager.spawnCube(Vector3(5f, 10f, 0f))
    }

    override fun render(delta: Float) {
        // Clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        // Update game state
        cubeManager.update(delta)
        uiManager.update(delta)

        // Render 3D elements
        environmentManager.beginRender()
        cubeManager.render(environmentManager.getModelBatch(), environmentManager.getEnvironment())
        environmentManager.endRender()

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (x in 0..10) {
            shapeRenderer.line(x.toFloat(), 0f, x.toFloat(), 20f)
        }
        for (y in 0..20) {
            shapeRenderer.line(0f, y.toFloat(), 10f, y.toFloat())
        }
        shapeRenderer.end()

        // Render UI
        uiManager.render()
    }

    override fun resize(width: Int, height: Int) {
        environmentManager.resize(width, height)
        uiManager.resize(width, height)
    }

    override fun dispose() {
        environmentManager.dispose()
        shapeRenderer.dispose()
        cubeManager.dispose()
        uiManager.dispose()
    }
}
