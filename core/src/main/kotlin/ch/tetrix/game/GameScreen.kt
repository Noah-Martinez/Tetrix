package ch.tetrix.game

import ch.tetrix.Game
import ch.tetrix.game.components.GameComponent
import ch.tetrix.game.services.GameService
import ch.tetrix.mainmenu.MainMenuScreen
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
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

val GAME_VIEWPORT_WIDTH = Gdx.graphics.width.toFloat()
val GAME_VIEWPORT_HEIGHT = Gdx.graphics.height.toFloat()

data class ComponentBackground(val drawable: Drawable)
data class ValueBackground(val drawable: Drawable)

/**
 * Main game screen that coordinates the different components of the game.
 */
class GameScreen(val context: Context) : TxScreen() {
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

    private fun exit() {
        game.removeScreen<GameScreen>()
        game.addScreen(MainMenuScreen(context))
        game.setScreen<MainMenuScreen>()
    }

    override fun render(delta: Float) {
        super.render(delta)
        gameComponent.act(delta)
        gameComponent.draw()
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
}
