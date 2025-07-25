package ch.tetrix.menu

import ch.tetrix.Game
import ch.tetrix.game.GameScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import ktx.actors.onClick
import ktx.inject.Context
import ktx.scene2d.Scene2DSkin

class MainMenuUI(context: Context) : Table(), Disposable {
    private val game: Game = context.inject()
    private var listeners = mutableListOf<EventListener>()

    init {
        setupMainMenuOverlay()
    }

    private fun setupMainMenuOverlay() {
        setFillParent(true)
        background = createOverlayBackground()

        val titleLabel = Label("TETRIX", Scene2DSkin.defaultSkin)
        titleLabel.setFontScale(2f) // Make title larger

        val playButton = TextButton("PLAY", Scene2DSkin.defaultSkin)
        val settingsButton = TextButton("SETTINGS", Scene2DSkin.defaultSkin)
        val exitButton = TextButton("EXIT", Scene2DSkin.defaultSkin)

        add(titleLabel).padBottom(50f).row()
        add(playButton).width(200f).height(50f).padBottom(15f).row()
        add(settingsButton).width(200f).height(50f).padBottom(15f).row()
        add(exitButton).width(200f).height(50f)

        listeners.addAll(arrayOf(
            playButton.onClick {
                game.setScreen<GameScreen>()
            },
            settingsButton.onClick {
                // TODO: Navigate to settings screen or show settings overlay
                println("Settings clicked - implement settings screen")
            },
            exitButton.onClick {
                Gdx.app.exit()
            },
        ))
    }

    private fun createOverlayBackground(): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0.8f) // Semi-transparent black
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(TextureRegion(texture))
    }

    override fun dispose() {
        listeners.forEach { removeListener(it) }
    }
}
