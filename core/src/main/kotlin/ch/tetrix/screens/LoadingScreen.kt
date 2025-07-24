package ch.tetrix.screens

import ch.tetrix.Game
import ch.tetrix.ui.LoadingOverlay
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import ktx.app.KtxScreen
import ktx.inject.Context

class LoadingScreen(val context: Context) : KtxScreen {
    private val game: Game = context.inject()
    private val batch: Batch = context.inject()
    private val assets: AssetManager = context.inject()
    private val camera: OrthographicCamera = context.inject()

    private val loadingOverlay = LoadingOverlay(context)
    private var loadingComplete = false

    override fun show() {
        loadAssets()

        loadingOverlay.show()
        loadingOverlay.addClickListener {
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

        loadingOverlay.updateProgress(progress, assetsFinished)

        camera.update()
        batch.projectionMatrix = camera.combined

        loadingOverlay.render(delta)

        if (assetsFinished && !loadingComplete) {
            loadingComplete = true
            loadingOverlay.setLoadingComplete()
        }
    }

    private fun navigateToMainMenu() {
        dispose()
        game.removeScreen<LoadingScreen>()
        game.setScreen<MainMenuScreen>()
    }


    override fun resize(width: Int, height: Int) {
        loadingOverlay.resize(width, height)
    }

    override fun dispose() {
        loadingOverlay.dispose()
    }
}
