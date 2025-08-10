package ch.tetrix.mainmenu.screens

import ch.tetrix.Game
import ch.tetrix.game.screens.GameScreen
import ch.tetrix.mainmenu.actions.MainMenuAction
import ch.tetrix.mainmenu.components.MainMenuViewBuilder
import ch.tetrix.optionmenu.screens.OptionMenuScreen
import ch.tetrix.scoreboard.screens.ScoreboardScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin

class MainMenuScreen(private val context: Context) : TxScreen() {
    private val screenViewport = ScreenViewport()
    private val batch: Batch = context.inject()
    private val game: Game = context.inject()

    override val stage: Stage by lazy { Stage(screenViewport, batch) }
    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }
    private val mainMenuLayout: KTableWidget by lazy { createMainMenuLayout(Scene2DSkin.defaultSkin) }

    companion object {
        private val log = logger<MainMenuScreen>()
    }

    override fun show() {
        super.show()
        inputMultiplexer.addProcessor(stage)
        stage.addActor(mainMenuLayout)
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
    }

    private fun createMainMenuLayout(skin: Skin): KTableWidget {
        return MainMenuViewBuilder.layout(
            skin = skin,
            onMenuAction = ::handleMenuAction
        )
    }

    private fun handleMenuAction(action: MainMenuAction) {
        when (action) {
            MainMenuAction.StartGame -> startGame()
            MainMenuAction.Options -> showOptionsMenu()
            MainMenuAction.Scores -> showScoresMenu()
            MainMenuAction.Credits -> showCreditsMenu()
            MainMenuAction.ExitGame -> exitGame()
        }
    }

    private fun startGame() {
        log.info { "Starting game..." }
        game.removeScreen<MainMenuScreen>()
        game.setScreen<GameScreen>()
    }

    private fun showOptionsMenu() {
        log.info { "Opening options..." }
        game.removeScreen<MainMenuScreen>()
        game.setScreen<OptionMenuScreen>()
    }

    private fun showScoresMenu() {
        game.removeScreen<MainMenuScreen>()
        game.setScreen<ScoreboardScreen>()
        log.info { "Opening scores..." }
    }

    private fun showCreditsMenu() {
        log.info { "Opening credits..." }
        // TODO: Show credits screen
    }

    private fun exitGame() {
        log.info { "Exiting game..." }
        Gdx.app.exit()
    }
}
