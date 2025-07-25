package ch.tetrix

import ch.tetrix.assets.FontAssets
import ch.tetrix.assets.SkinAssets
import ch.tetrix.assets.load
import ch.tetrix.loading.LoadingScreen
import ch.tetrix.menu.MainMenuScreen
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.freetype.registerFreeTypeFontLoaders
import ktx.inject.Context
import ktx.inject.register
import ktx.log.logger
import ktx.scene2d.Scene2DSkin

private val log = logger<Game>()

object GameConstants {
    const val WORLD_WIDTH  = 10f
    const val WORLD_HEIGHT = 20f
}

class Game : KtxGame<KtxScreen>() {
    // Global context for dependency injection
    private val context = Context()
    private val assets = AssetManager()

    private val defaultFont = assets.load(FontAssets.Default)

    override fun create() {
        super.create()
        contextRegister()

        addScreen(LoadingScreen(context))
        addScreen(MainMenuScreen(context))

        setScreen<LoadingScreen>()
    }

    private fun contextRegister() {
        context.register {

            bindSingleton<Game>(this@Game)
            bindSingleton<Batch>(SpriteBatch())
            bindSingleton<AssetManager>(assets)
            bindSingleton<PooledEngine>(PooledEngine())
            bindSingleton<OrthographicCamera>(OrthographicCamera().apply {
                setToOrtho(true)
            })

            // load font asset:
            assets.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)
            defaultFont.finishLoading()
            bindSingleton<BitmapFont>(defaultFont.asset)

            // load default skin asset:
            val defaultSkin = assets.load(SkinAssets.Default)
            defaultSkin.finishLoading()
            Scene2DSkin.defaultSkin = defaultSkin.asset

            // set input multiplexer
            val inputMultiplexer = InputMultiplexer()
            Gdx.input.inputProcessor = inputMultiplexer
            bindSingleton<InputMultiplexer>(inputMultiplexer)
        }
    }

    override fun dispose() {
        super.dispose()
        log.debug { "Entities in engine: ${context.inject<PooledEngine>().entities.size()}" }
        context.remove<Game>() // prevent self call
        context.dispose()
    }
}
