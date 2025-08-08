package ch.tetrix.scoreboard.screens

import ch.tetrix.Game
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.scoreboard.components.ScoreboardViewBuilder
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.inject.Context

class ScoreboardScreen(private val context: Context): TxScreen() {
    val game: Game by lazy { context.inject() }
    val inputMultiplexer: InputMultiplexer by lazy { context.inject() }

    override val stage by lazy {
        Stage(
            FitViewport(
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )
        )
    }

    override fun show() {
        super.show()

        inputMultiplexer.addProcessor(stage)

        stage.addActor(ScoreboardViewBuilder.layout(context) {
            game.removeScreen<ScoreboardScreen>()
            game.setScreen<MainMenuScreen>()
        })
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
}
