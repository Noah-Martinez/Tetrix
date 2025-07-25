package ch.tetrix.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.inject.Context

class MainMenuScreen(val context: Context) : KtxScreen {
    private lateinit var inputMultiplexer: InputMultiplexer
    private lateinit var stage: Stage
    private lateinit var menuUI: MainMenuUI

    override fun show() {
        inputMultiplexer = context.inject()

        stage = Stage(ScreenViewport())
        inputMultiplexer.addProcessor(stage)

        menuUI = MainMenuUI(context)
        stage.addActor(menuUI)
    }

    override fun render(delta: Float) {
        // Clear the screen with a dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Render the overlay
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(stage)
        stage.dispose()
        menuUI.dispose()
    }
}
