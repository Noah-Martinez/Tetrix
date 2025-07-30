package ch.tetrix.mainmenu

import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.inject.Context
import ktx.log.logger

class MainMenuScreen(private val context: Context) : TxScreen() {
    private val screenViewport = ScreenViewport()
    private val batch: Batch = context.inject()

    override val stage: Stage by lazy { Stage(screenViewport, batch) }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }

    companion object {
        private val log = logger<MainMenuScreen>()
    }

    override fun show() {
        inputMultiplexer.addProcessor(stage)
        stage.addActor(createMainMenuLayout(skin, log, context))
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
    }
}
