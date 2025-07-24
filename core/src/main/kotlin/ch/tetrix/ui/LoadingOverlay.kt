package ch.tetrix.ui

import BaseOverlay
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.inject.Context
import ktx.scene2d.Scene2DSkin

class LoadingOverlay(val context: Context) : BaseOverlay() {
    private val inputMultiplexer: InputMultiplexer = context.inject()

    private lateinit var progressBar: ProgressBar
    private lateinit var statusLabel: Label
    private lateinit var percentLabel: Label
    private lateinit var welcomeLabel: Label
    private lateinit var waitLabel: Label

    init {
        inputMultiplexer.addProcessor(stage)
        stage.viewport = ScreenViewport()
        setupUI()
    }

    private fun setupUI() {
        stage.clear()

        // Create all labels first
        welcomeLabel = Label("Welcome to Tetrix!!!", Scene2DSkin.defaultSkin, "title")
        statusLabel = Label("Loading assets...", Scene2DSkin.defaultSkin, "default")
        percentLabel = Label("0%", Scene2DSkin.defaultSkin, "default")
        waitLabel = Label("Please wait...", Scene2DSkin.defaultSkin, "default")

        progressBar = ProgressBar(0f, 1f, 0.01f, false, Scene2DSkin.defaultSkin)
        progressBar.setAnimateDuration(0.1f)

        val table = Table()
        table.setFillParent(true)
        table.center()
        table.defaults().pad(10f)

        table.add(welcomeLabel).colspan(2).padBottom(30f).row()
        table.add(statusLabel).colspan(2).padBottom(10f).row()
        table.add(progressBar).expandX().center().width(300f).height(20f).padBottom(10f).row()
        table.add(percentLabel).colspan(2).padBottom(20f).row()
        table.add(waitLabel).colspan(2)

        stage.addActor(table)
        stage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
    }

    fun updateProgress(progress: Float, assetsFinished: Boolean) {
        progressBar.value = progress
        val percentage = (progress * 100).toInt()
        percentLabel.setText("$percentage%")

        val statusText = when {
            !assetsFinished && progress < 0.3f -> "Loading textures..."
            !assetsFinished && progress < 0.6f -> "Loading audio..."
            !assetsFinished && progress < 0.9f -> "Loading game data..."
            !assetsFinished -> "Finalizing assets..."
            else -> "Complete!"
        }

        if (statusLabel.text.toString() != statusText) {
            statusLabel.setText(statusText)
            statusLabel.invalidateHierarchy()
        }
    }

    fun setLoadingComplete() {
        waitLabel.setText("Click anywhere to continue!")
        statusLabel.setText("Ready!")
    }

    fun addClickListener(onClickAction: () -> Unit) {
        stage.addListener { event ->
            if (event.toString().contains("touchDown") || event.toString().contains("keyDown")) {
                onClickAction()
                true
            } else {
                false
            }
        }
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
    }
}
