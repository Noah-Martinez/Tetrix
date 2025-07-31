package ch.tetrix.game

import ch.tetrix.Game
import ch.tetrix.game.components.GameComponent
import ch.tetrix.game.services.GameService
import ch.tetrix.mainmenu.MainMenuScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger
import ktx.scene2d.Scene2DSkin

val GAME_VIEWPORT_WIDTH = Gdx.graphics.width.toFloat()
val GAME_VIEWPORT_HEIGHT = Gdx.graphics.height.toFloat()

data class ComponentBackground(val drawable: Drawable)
data class ValueBackground(val drawable: Drawable)

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : TxScreen() {
    private var isPaused: Boolean = false

    override val stage: Stage by lazy {
        // has to be fit viewport otherwise game field cells won't be 1:1 ratio
        val viewport = FitViewport(GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT)
        Stage(viewport)
    }

    private val game: Game by lazy { context.inject() }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }
    private val gameService: GameService by lazy { GameService() }

    private val componentBackground by lazy {
        val patch: NinePatch = skin.getPatch("table-background-round")
        patch.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        NinePatchDrawable(patch)
    }

    private val valueBackground by lazy {
        val patch: NinePatch = skin.getPatch("game-value-bg")
        patch.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        NinePatchDrawable(patch)
    }

    private val gameComponent by lazy { GameComponent(context) }
    private val gameLayout by lazy { GameViewBuilder.layout(context, gameComponent) }
    private val pauseOverlay by lazy {
        GamePauseViewBuilder.layout(
            skin = Scene2DSkin.defaultSkin,
            stage = stage,
            onPauseMenuAction = ::handlePauseMenuAction
        )
    }

    companion object {
        private val log = logger<GameScreen>()
    }

    override fun show() {
        context.register {
            bindSingleton<Skin>(skin)
            bindSingleton(gameService)
            bindSingleton(ComponentBackground(componentBackground))
            bindSingleton(ValueBackground(valueBackground))
        }

        stage.addActor(gameLayout)
        inputMultiplexer.addProcessor(stage)
    }

    override fun render(delta: Float) {
        super.render(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if(!isPaused){
                pauseGame()
            } else {
                resumeGame()
            }
        }

        if (!isPaused) {
            gameComponent.act(delta)
        }
        stage.act(delta)

        gameComponent.draw()
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        gameComponent.dispose()
        context.apply {
            remove<Skin>()
            remove<GameService>()
            remove<ComponentBackground>()
            remove<ValueBackground>()
        }
        inputMultiplexer.removeProcessor(stage)
    }

    private fun handlePauseMenuAction(action: GamePauseAction) {
        when (action) {
            GamePauseAction.Continue -> resumeGame()
            GamePauseAction.Options -> showOptionsMenu()
            GamePauseAction.MainMenu -> goToMainMenu()
        }
    }

    private fun pauseGame() {
        log.info { "Pause Game" }
        isPaused = true
        gameLayout.touchable = com.badlogic.gdx.scenes.scene2d.Touchable.disabled
        stage.addActor(pauseOverlay)
    }

    private fun resumeGame() {
        isPaused = false

        pauseOverlay.remove()
        gameLayout.touchable = com.badlogic.gdx.scenes.scene2d.Touchable.enabled
    }

    private fun showOptionsMenu() {
        log.info { "Show Option Menu" }
        // TODO Show option Menu
    }

    private fun goToMainMenu() {
        game.removeScreen<GameScreen>()
        game.addScreen(MainMenuScreen(context))
        game.setScreen<MainMenuScreen>()
    }

}
