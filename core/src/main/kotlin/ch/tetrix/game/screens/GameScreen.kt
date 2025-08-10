package ch.tetrix.game.screens

import ch.tetrix.GAME_HEIGHT
import ch.tetrix.GAME_WIDTH
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

data class ComponentBackground(val drawable: Drawable)
data class ValueBackground(val drawable: Drawable)

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : TxScreen() {
    private val game by lazy { context.inject<Game>() }
    private val inputMultiplexer by lazy { context.inject<InputMultiplexer>() }

    private val batch = SpriteBatch()

    private val viewport by lazy { FitViewport(GAME_WIDTH, GAME_HEIGHT) }
    override val stage by lazy { Stage(viewport, batch) }

    private val pauseStage by lazy {
        Stage(ExtendViewport(GAME_WIDTH, GAME_HEIGHT), batch) // Uses the entire screen
    }

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

        if(GameService.isGameOver.value){
            goToGameOverMenu()
        }

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

    private fun goToMainMenu() {
        game.removeScreen<GameScreen>()
        game.setScreen<MainMenuScreen>()
    }

    private fun goToGameOverMenu() {
        game.removeScreen<GameScreen>()
        game.setScreen<GameOverScreen>()
    }
}
