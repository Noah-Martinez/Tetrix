package ch.tetrix.game.screens

import ch.tetrix.GAME_HEIGHT
import ch.tetrix.GAME_WIDTH
import ch.tetrix.Game
import ch.tetrix.game.actions.GameOverAction
import ch.tetrix.game.components.GameOverViewBuilder
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin

class GameOverScreen(val context: Context, val score: Int) : TxScreen() {
    private val batch by lazy { context.inject<Batch>() }
    private val game by lazy { context.inject<Game>() }
    private val scoreRepository by lazy { context.inject<ScoreboardRepository>() }
    private val inputMultiplexer by lazy { context.inject<InputMultiplexer>() }

    private val viewport by lazy { FitViewport(GAME_WIDTH, GAME_HEIGHT) }
    override val stage by lazy { Stage(viewport, batch) }

    private val gameOverMenuLayout: KTableWidget by lazy {
        GameOverViewBuilder.layout(
            skin = Scene2DSkin.defaultSkin,
            scoreRepository.getGameOverScores(score),
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
