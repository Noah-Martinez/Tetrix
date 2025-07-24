package ch.tetrix.screens

import ch.tetrix.game.stages.GameField
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import ktx.inject.Context

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : KtxScreen {
    private lateinit var gameField: GameField

    override fun show() {
        super.show()
        gameField = GameField(17, 36, context)
        gameField.act()
        gameField.draw()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glDepthFunc(GL20.GL_LESS)
        Gdx.gl.glEnable(GL20.GL_CULL_FACE)
        Gdx.gl.glCullFace(GL20.GL_BACK)

        gameField.act(delta)
        gameField.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameField.resize(width, height)
    }

    override fun dispose() {
        if (::gameField.isInitialized) {
            gameField.dispose()
        }
    }
}
