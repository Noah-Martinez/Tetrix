package ch.tetrix.game

import ch.tetrix.GameConstants
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.utils.viewport.FitViewport

/**
 * Manages the 3D environment for the game.
 */
class EnvironmentManager(private var camera: OrthographicCamera) {
    private val modelBatch = ModelBatch()
    private val environment = Environment().apply {
        set(ColorAttribute.createAmbientLight(0.8f, 0.8f, 0.8f, 1f))
        add(DirectionalLight().set(Color.WHITE, 0f, -1f, -1f))
    }

    private val viewport = FitViewport(
        GameConstants.WORLD_WIDTH,
        GameConstants.WORLD_HEIGHT,
        camera
    )

    init {
        setupCamera()
    }

    private fun setupCamera() {
        this.camera.apply {
            position.set(5f, 10f, 10f)
            lookAt(5f, 10f, 0f)
            update()
        }
    }

    /**
     * Begins rendering with the model batch.
     */
    fun beginRender() {
        modelBatch.begin(camera)
    }

    /**
     * Ends rendering with the model batch.
     */
    fun endRender() {
        modelBatch.end()
    }

    /**
     * Gets the model batch for rendering.
     */
    fun getModelBatch(): ModelBatch = modelBatch

    /**
     * Gets the environment for rendering.
     */
    fun getEnvironment(): Environment = environment

    /**
     * Handles screen resize events.
     */
    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    /**
     * Disposes of resources used by the environment manager.
     */
    fun dispose() {
        modelBatch.dispose()
    }
}
