package ch.tetrix.screens

import ch.tetrix.ui.MainMenuOverlay
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import ktx.inject.Context

class MainMenuScreen(val context: Context) : KtxScreen {
    private lateinit var mainMenuOverlay: MainMenuOverlay

    override fun show() {
        // Initialize the overlay when the screen is shown
        mainMenuOverlay = MainMenuOverlay(context)
        mainMenuOverlay.show()
    }

    override fun render(delta: Float) {
        // Clear the screen with a dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Render the overlay
        mainMenuOverlay.render(delta)
    }

    override fun resize(width: Int, height: Int) {
        mainMenuOverlay.resize(width, height)
    }

    override fun hide() {
        mainMenuOverlay.hide()
    }

    override fun dispose() {
        if (::mainMenuOverlay.isInitialized) {
            mainMenuOverlay.dispose()
        }
    }
}
