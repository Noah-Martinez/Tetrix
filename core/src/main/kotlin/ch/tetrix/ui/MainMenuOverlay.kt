package ch.tetrix.ui

import BaseOverlay
import ch.tetrix.Game
import ch.tetrix.screens.GameScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.scene2d.Scene2DSkin

class MainMenuOverlay(
    private val assets: AssetManager,
    private val game: Game
) : BaseOverlay() {

    private lateinit var mainTable: Table

    init {
        setupMainMenuOverlay()
    }

    private fun setupMainMenuOverlay() {
        val background = Table()
        background.setFillParent(true)
        background.background = createOverlayBackground()
        stage.addActor(background)

        mainTable = Table()
        mainTable.setFillParent(true)

        val titleLabel = Label("TETRIX", Scene2DSkin.defaultSkin)
        titleLabel.setFontScale(2f) // Make title larger

        val playButton = TextButton("PLAY", Scene2DSkin.defaultSkin)
        val settingsButton = TextButton("SETTINGS", Scene2DSkin.defaultSkin)
        val exitButton = TextButton("EXIT", Scene2DSkin.defaultSkin)

        mainTable.add(titleLabel).padBottom(50f).row()
        mainTable.add(playButton).width(200f).height(50f).padBottom(15f).row()
        mainTable.add(settingsButton).width(200f).height(50f).padBottom(15f).row()
        mainTable.add(exitButton).width(200f).height(50f)

        stage.addActor(mainTable)

        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen<GameScreen>()
            }
        })

        settingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // TODO: Navigate to settings screen or show settings overlay
                println("Settings clicked - implement settings screen")
            }
        })

        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })
    }

    private fun createOverlayBackground(): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0.8f) // Semi-transparent black
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(TextureRegion(texture))
    }
}
