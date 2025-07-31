package ch.tetrix.loading

import ch.tetrix.Game
import ch.tetrix.mainmenu.MainMenuScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.inject.Context

class LoadingScreen(private val context: Context) : TxScreen() {
    private val game: Game = context.inject()
    private val batch: Batch = context.inject()
    private val assets: AssetManager = context.inject()
    private val camera: OrthographicCamera = context.inject()

    private val screenViewport = ScreenViewport()
    override val stage = Stage(screenViewport, batch)
    private val inputMultiplexer: InputMultiplexer = context.inject()

    private lateinit var loadingUI: LoadingUI
    private var loadingComplete = false

    override fun show() {
        inputMultiplexer.addProcessor(stage)
        loadingUI = LoadingUI(context)
        stage.addActor(loadingUI)
        loadingUI.addClickListener {
            navigateToMainMenu()
        }
    }

    override fun render(delta: Float) {
        super.render(delta)
        val assetsFinished = assets.update()
        val progress = assets.progress

        loadingUI.updateProgress(progress, assetsFinished)

        camera.update()
        batch.projectionMatrix = camera.combined

        if (assetsFinished && !loadingComplete) {
            loadingComplete = true
            loadingUI.setLoadingComplete()
        }

        stage.act(delta)
        stage.draw()
    }

    private fun navigateToMainMenu() {
        game.removeScreen<LoadingScreen>()
        game.setScreen<MainMenuScreen>()
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
    }
}
