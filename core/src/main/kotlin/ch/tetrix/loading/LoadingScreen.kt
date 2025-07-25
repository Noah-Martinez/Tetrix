package ch.tetrix.loading

import ch.tetrix.Game
import ch.tetrix.game.GameScreen
import ch.tetrix.menu.MainMenuScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.inject.Context

class LoadingScreen(val context: Context) : KtxScreen {
    private val game: Game = context.inject()
    private val batch: Batch = context.inject()
    private val assets: AssetManager = context.inject()
    private val camera: OrthographicCamera = context.inject()
    private val stage = Stage(ScreenViewport())
    private val inputMultiplexer: InputMultiplexer = context.inject()

    private lateinit var loadingUI: LoadingUI
    private var loadingComplete = false

    override fun show() {
        loadAssets()

        inputMultiplexer.addProcessor(stage)
        loadingUI = LoadingUI(context)
        stage.addActor(loadingUI)
        loadingUI.addClickListener {
            navigateToMainMenu()
        }
    }

    private fun loadAssets() {
        // MusicAssets.entries.forEach { assets.load(it) }
        // SoundAssets.entries.forEach { assets.load(it) }
        // TextureAtlasAssets.entries.forEach { assets.load(it) }

        // assets.load("textures/player.png", Texture::class.java)
        // assets.load("audio/background.mp3", Music::class.java)
        // assets.load("sounds/click.wav", Sound::class.java)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val assetsFinished = assets.update()
        val progress = assets.progress

        loadingUI.updateProgress(progress, assetsFinished)

        camera.update()
        batch.projectionMatrix = camera.combined
        stage.act(delta)
        stage.draw()

        if (assetsFinished && !loadingComplete) {
            loadingComplete = true
            loadingUI.setLoadingComplete()
        }
    }

    private fun navigateToMainMenu() {
        dispose()
        game.addScreen(GameScreen(context))
        game.removeScreen<LoadingScreen>()
        game.setScreen<MainMenuScreen>()
    }


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(stage)
        stage.dispose()
    }
}
