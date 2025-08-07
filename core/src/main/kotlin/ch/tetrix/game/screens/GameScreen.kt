package ch.tetrix.game.screens

import ch.tetrix.Game
import ch.tetrix.game.actions.GamePauseAction
import ch.tetrix.game.components.GamePauseViewBuilder
import ch.tetrix.game.components.GameViewBuilder
import ch.tetrix.game.services.GameService
import ch.tetrix.game.stages.GameStage
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger
import ktx.scene2d.Scene2DSkin

const val GAME_VIEWPORT_WIDTH = 600f
const val GAME_VIEWPORT_HEIGHT = 500f

data class ComponentBackground(val drawable: Drawable)
data class ValueBackground(val drawable: Drawable)

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : TxScreen() {
    private val batch = SpriteBatch()

    override val stage: Stage by lazy {
        Stage(FitViewport(GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT), batch) // Use FitViewport here
    }

    private val pauseStage: Stage by lazy {
        Stage(ExtendViewport(GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT), batch) // Uses the entire screen
    }

    private val game: Game by lazy { context.inject() }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }

    private val componentBackground by lazy { skin.getDrawable("table-background-round") }
    private val valueBackground by lazy { skin.getDrawable("game-value-bg") }

    private val gameStage by lazy { GameStage(context, batch) }
    private val gameLayout by lazy { GameViewBuilder.layout(context, gameStage) }

    private val pauseOverlay by lazy {
        GamePauseViewBuilder.layout(
            skin = Scene2DSkin.defaultSkin,
            onPauseMenuAction = ::handlePauseMenuAction
        )
    }

    init {
        if(!game.containsScreen<GameOverScreen>()) {
            game.addScreen(GameOverScreen(context))
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }

    override fun show() {
        context.register {
            bindSingleton(ComponentBackground(componentBackground))
            bindSingleton(ValueBackground(valueBackground))
        }

        stage.addActor(gameLayout)
        inputMultiplexer.addProcessor(stage)
        GameService.startNewGame(context, gameStage)
    }

    override fun render(delta: Float) {
        super.render(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if(!GameService.isGamePaused.value){
                pauseGame()
            } else {
                resumeGame()
            }
        }

        if (!GameService.isGamePaused.value) {
            stage.act(delta)
            gameStage.act(delta)
        } else {
            pauseStage.act(delta)
        }

        stage.viewport.apply()
        stage.draw()
        gameStage.draw()

        if(GameService.isGamePaused.value) {
            pauseStage.viewport.apply()
            pauseStage.draw()
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        gameStage.viewport.update(width, height, false)
        stage.viewport.update(width, height, false)
        pauseStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        super.dispose()
        pauseStage.dispose()
        gameStage.dispose()
        GameService.endGame()
        GameService.disposeAllCubes()
        context.apply {
            remove<GameService>()
            remove<ComponentBackground>()
            remove<ValueBackground>()
        }
        batch.dispose()
        inputMultiplexer.clear()
    }

    private fun handlePauseMenuAction(action: GamePauseAction) {
        when (action) {
            GamePauseAction.Continue -> resumeGame()
            GamePauseAction.Options -> showOptionsMenu()
            GamePauseAction.MainMenu -> goToMainMenu()
        }
    }

    private fun pauseGame() {
        GameService.pauseGame()

        pauseStage.addActor(pauseOverlay)
        inputMultiplexer.addProcessor(pauseStage)
    }

    private fun resumeGame() {
        pauseStage.clear()
        inputMultiplexer.removeProcessor(pauseStage)

        GameService.resumeGame()
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
