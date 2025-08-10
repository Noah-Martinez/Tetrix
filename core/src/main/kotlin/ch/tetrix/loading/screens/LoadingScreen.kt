package ch.tetrix.loading.screens

import ch.tetrix.GAME_HEIGHT
import ch.tetrix.GAME_WIDTH
import ch.tetrix.Game
import ch.tetrix.assets.AudioAssets
import ch.tetrix.assets.TextureAssets
import ch.tetrix.assets.load
import ch.tetrix.assets.loadClamped
import ch.tetrix.loading.components.LoadingViewBuilder
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin

class LoadingScreen(context: Context) : TxScreen() {
    private val game by lazy { context.inject<Game>() }
    private val batch by lazy { context.inject<Batch>() }
    private val assets by lazy { context.inject<AssetManager>() }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }

    private val viewport by lazy { FitViewport(GAME_WIDTH, GAME_HEIGHT) }
    override val stage by lazy { Stage(viewport, batch) }

    private var assetsLoaded: Boolean = false

    private lateinit var progressBar: ProgressBar
    private lateinit var promptLabel: Label
    private lateinit var assetLabel: Label
    private lateinit var statusLabel: Label

    private val loadingLayout: KTableWidget by lazy {
        val result = createLoadingLayout(Scene2DSkin.defaultSkin)
        progressBar = result.progressBar
        promptLabel = result.promptLabel
        assetLabel = result.assetLabel
        statusLabel = result.statusLabel
        result.tableWidget
    }

    private val inputProcessor = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean = onAnyKeyPressed()
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = onAnyKeyPressed()
    }

    override fun show() {
        inputMultiplexer.addProcessor(inputProcessor)
        inputMultiplexer.addProcessor(stage)
        stage.addActor(loadingLayout)

        loadAssets()
    }

    private fun loadAssets() {
        AudioAssets.entries.forEach { assets.load(it) }
        TextureAssets.entries.forEach { assets.loadClamped(it) }
    }

    override fun render(delta: Float) {
        super.render(delta)

        progressBar.value = assets.progress

        if (!assets.isFinished) {
            statusLabel.setText("Loading assets... ${assets.loadedAssets}/${assets.queuedAssets}")

            assets.getDiagnostics()?.let { diagnostics ->
                val loadingLine = diagnostics.lines().find { it.trim().startsWith("Loading:") }
                if (loadingLine != null) {
                    val path = loadingLine.substringAfter("Loading:").substringBefore(',').trim()
                    val fileName = path.substringAfterLast('/')
                    assetLabel.setText("Loading: $fileName")
                }
            }
        }

        if (assets.update() && !assetsLoaded) {
            assetsLoaded = true

            statusLabel.setText("Loading complete!")
            promptLabel.isVisible = true
            assetLabel.isVisible = false
        }

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        stage.viewport.update(width, height, true)
    }

    private fun navigateToMainMenu() {
        game.removeScreen<LoadingScreen>()
        game.setScreen<MainMenuScreen>()
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
        inputMultiplexer.removeProcessor(inputProcessor)
    }

    private fun createLoadingLayout(skin: Skin): LoadingViewBuilder.LoadingLayoutResult {
        return LoadingViewBuilder.layout(
            skin = skin,
        )
    }

    private fun onAnyKeyPressed(): Boolean {
        if (assets.isFinished) {
            navigateToMainMenu()
            return true
        }
        return false
    }
}
