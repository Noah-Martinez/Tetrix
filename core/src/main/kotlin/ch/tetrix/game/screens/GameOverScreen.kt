package ch.tetrix.game.screens

import ch.tetrix.Game
import ch.tetrix.game.actions.GameOverAction
import ch.tetrix.game.components.GameOverViewBuilder
import ch.tetrix.game.services.GameService
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin

class GameOverScreen(val context: Context) : TxScreen() {
    private val screenViewport = ScreenViewport()
    private val batch: Batch = context.inject()
    private val game: Game = context.inject()
    private val scoreRepository: ScoreboardRepository = context.inject()

    override val stage: Stage by lazy { Stage(screenViewport, batch).apply {
    } }

    private val inputMultiplexer: InputMultiplexer by lazy { context.inject() }
    private val gameOverMenuLayout: KTableWidget by lazy {
        GameOverViewBuilder.layout(
            skin = Scene2DSkin.defaultSkin,
            scoreRepository.getGameOverScores(GameService.score.value),
            stage =  stage,
            onGameOverAction = ::handleGameOverAction
        )
    }

    companion object {
        private val log = logger<GameOverScreen>()
    }

    override fun show() {
        super.show()
        inputMultiplexer.addProcessor(stage)
        stage.addActor(gameOverMenuLayout)
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

    private fun handleGameOverAction(action: GameOverAction) {
        when (action) {
            is GameOverAction.UsernameConfirmation -> onUsernameConfirmation(action.score)
            is GameOverAction.MainMenu -> goToMainMenu()
        }
    }

    private fun onUsernameConfirmation(score: ScoreDto) {
        scoreRepository.addScore(score.toScoreEntity())
        log.info { "Saved score: $score" }
        goToMainMenu()
    }

    private fun goToMainMenu() {
        game.removeScreen<GameOverScreen>()
        game.setScreen<MainMenuScreen>()
    }
}
