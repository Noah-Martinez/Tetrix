package ch.tetrix.ui

import ch.tetrix.GameConstants
import ch.tetrix.assets.SkinAssets
import ch.tetrix.game.CubeManager
import ch.tetrix.get
import ch.tetrix.load
import ch.tetrix.scoreboard.ScoreboardFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport

/**
 * Manages the UI elements of the game.
 */
class UIManager(
    private val cubeManager: CubeManager,
    private val asset: AssetManager,
) {
    private val stage = Stage(FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT))
    private val skin: Skin

    init {
        // TODO: load all necessary assets at one point, maybe using a splashscreen with an image only
        asset.load(SkinAssets.Default).finishLoading()
        skin = asset[SkinAssets.Default]
        setupUI()
        Gdx.input.inputProcessor = InputMultiplexer(stage)
    }

    /**
     * Sets up the UI elements and their event handlers.
     */
    private fun setupUI() {
        val leftButton = TextButton("Left", skin).pad(10f)
        val rightButton = TextButton("Right", skin).pad(10f)
        val addScoreBtn = TextButton("Add Score", skin).pad(10f)
        val getScoresBtn = TextButton("Get Scores", skin).pad(10f)

        val table = Table().apply {
            setFillParent(true)
            bottom().pad(20f)
            add(leftButton).pad(20f)
            add(rightButton).pad(20f)
            add(addScoreBtn).pad(20f)
            add(getScoresBtn).pad(20f)
        }

        stage.addActor(table)

        // Move cube left
        leftButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                cubeManager.moveActiveLeft()
            }
        })

        // Move cube right
        rightButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                cubeManager.moveActiveRight()
            }
        })

        // Add random score
        addScoreBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val charPool: List<Char> = ('A'..'Z') + ('a'..'z')
                val name = (1..5)
                    .map { charPool.random() }
                    .joinToString("")

                ScoreboardFactory.getScoreboard(type = ScoreboardFactory.StorageType.CSV).addScore(name)
                ScoreboardFactory.getScoreboard(type = ScoreboardFactory.StorageType.DATABASE).addScore(name)
            }
        })

        // Get and print scores
        getScoresBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                println("CSV: ${ScoreboardFactory.getScoreboard(type = ScoreboardFactory.StorageType.CSV).getScores()}")
                println("DATABASE: ${ScoreboardFactory.getScoreboard(type = ScoreboardFactory.StorageType.DATABASE).getScores()}")
            }
        })
    }

    /**
     * Updates the UI based on the time delta.
     */
    fun update(delta: Float) {
        stage.act(delta)
    }

    /**
     * Renders the UI.
     */
    fun render() {
        stage.draw()
    }

    /**
     * Handles screen resize events.
     */
    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    /**
     * Disposes of resources used by the UI manager.
     */
    fun dispose() {
        stage.dispose()
    }
}
