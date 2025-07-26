package ch.tetrix.mainmenu

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.Scene2DSkin

class MainMenuScreen(private val context: Context) : KtxScreen {
    private val screenViewport = ScreenViewport()
    private val batch: Batch = context.inject()

    private val stage: Stage by lazy { Stage(screenViewport, batch) }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }

    companion object {
        private val log = logger<MainMenuScreen>()
    }

    override fun show() {
        inputMultiplexer.addProcessor(stage)
        stage.addActor(createMainMenuLayout(Scene2DSkin.defaultSkin, log))
    }

    override fun render(delta: Float) {
        val backgroundColor = Scene2DSkin.defaultSkin.getColor("primary")
        clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)

        stage.viewport.apply()
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}
