package ch.tetrix.loading

import ch.tetrix.Game
import ch.tetrix.mainmenu.MainMenuScreen
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.inject.Context
import ktx.scene2d.Scene2DSkin

class LoadingScreen(private val context: Context) : KtxScreen {
    private val game: Game = context.inject()
    private val batch: Batch = context.inject()
    private val assets: AssetManager = context.inject()
    private val camera: OrthographicCamera = context.inject()

    private val screenViewport = ScreenViewport()

    private val stage = Stage(screenViewport, batch)
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
        val backgroundColor = Scene2DSkin.defaultSkin.getColor("primary")
        clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)

        val assetsFinished = assets.update()
        val progress = assets.progress

        loadingUI.updateProgress(progress, assetsFinished)

        camera.update()
        batch.projectionMatrix = camera.combined

        stage.viewport.apply()
        stage.act(delta)
        stage.draw()

        if (assetsFinished && !loadingComplete) {
            loadingComplete = true
            loadingUI.setLoadingComplete()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    private fun navigateToMainMenu() {
        game.removeScreen<LoadingScreen>()
        dispose()
        game.setScreen<MainMenuScreen>()
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
        inputMultiplexer.removeProcessor(stage)
    }
}
