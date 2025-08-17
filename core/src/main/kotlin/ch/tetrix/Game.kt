package ch.tetrix

import ch.tetrix.assets.SkinAssets
import ch.tetrix.assets.load
import ch.tetrix.loading.screens.LoadingScreen
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.scoreboard.services.ScoreboardService
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxGame
import ktx.async.KtxAsync
import ktx.freetype.registerFreeTypeFontLoaders
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger
import ktx.scene2d.Scene2DSkin

const val GAME_WIDTH = 600f
const val GAME_HEIGHT = 500f

class Game : KtxGame<TxScreen>() {
    private val batch: Batch by lazy { SpriteBatch() }

    val context = Context()
    private val assets = AssetManager()

    companion object {
        private val log = logger<Game>()
    }

    init {
        assets.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)
    }

    override fun create() {
        super.create()
        log.info { "Initializing Game: TETRIX" }
        KtxAsync.initiate()

        contextRegister()

        setScreen<LoadingScreen>()
    }

    private fun contextRegister() {
        context.register {
            bindSingleton<Game>(this@Game)
            bindSingleton<Batch>(batch)
            bindSingleton<AssetManager>(assets)
            bindSingleton<OrthographicCamera>(OrthographicCamera().apply {
                setToOrtho(true)
            })

            // load default skin asset:
            val defaultSkin = assets.load(SkinAssets.Default)
            defaultSkin.finishLoading()
            Scene2DSkin.defaultSkin = defaultSkin.asset
            bindSingleton<Skin>(Scene2DSkin.defaultSkin)

            // set input multiplexer
            val inputMultiplexer = InputMultiplexer()
            Gdx.input.inputProcessor = inputMultiplexer
            bindSingleton<InputMultiplexer>(inputMultiplexer)

            bindSingleton<ScoreboardRepository>(
                ScoreboardService.getScoreboard(
                    ScoreboardService.StorageType.CSV
                )
            )
        }
    }

    override fun <Type : TxScreen> removeScreen(type: Class<Type>): Type? {
        val screen = super.removeScreen(type)
        screen?.dispose()
        return screen
    }


    /**
     * Sets the current screen for the game if it exists. If the specified screen does not exist,
     * it creates an instance of the screen, adds it to the game, and sets it as the current screen.
     *
     * @param type The class type of the screen to be set. The screen should extend the [TxScreen] class.
     * The type parameter ensures only valid screens are processed.
     */
    override fun <Type : TxScreen> setScreen(type: Class<Type>) {
        if (containsScreen(type)) {
            super.setScreen(type)
        } else {
            val constructor = type.getConstructor(Context::class.java)
            val newScreen = constructor.newInstance(context)
            addScreen(type, newScreen)
            log.info { "Screen ${type.simpleName} was added to Game." }

            super.setScreen(type)
        }
    }

    override fun dispose() {
        super.dispose()
        context.clear()
    }
}
