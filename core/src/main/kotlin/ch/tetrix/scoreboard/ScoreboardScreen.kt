package ch.tetrix.scoreboard

import ch.tetrix.Game
import ch.tetrix.mainmenu.MainMenuScreen
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
            game.addScreen(MainMenuScreen(context))
            game.setScreen<MainMenuScreen>()
        })
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.removeProcessor(stage)
    }
}
