package ch.tetrix

import ch.tetrix.assets.FontAssets
import ch.tetrix.assets.SkinAssets
import ch.tetrix.screens.GameScreen
import ch.tetrix.screens.LoadingScreen
import ch.tetrix.screens.MainMenuScreen
import com.badlogic.ashley.core.PooledEngine
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
    private val context = Context()
    private val assets = AssetManager()

    override fun create() {
        context.register {
            bindSingleton(assets)

            assets.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)
            val defaultFont = assets.load(FontAssets.Default)
            defaultFont.finishLoading()
            bindSingleton<BitmapFont>(defaultFont.asset)

            bindSingleton<Batch>(SpriteBatch())
            bindSingleton(OrthographicCamera().apply {
                setToOrtho(false, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT)
            })
            bindSingleton(PooledEngine())

            val defaultSkin = assets.load(SkinAssets.Default)
            defaultSkin.finishLoading()
            Scene2DSkin.defaultSkin = defaultSkin.asset

            addScreen(LoadingScreen(this@Game, inject(), inject(), inject()))
            addScreen(GameScreen(inject(), inject(), inject(), inject(), inject()))
            addScreen(MainMenuScreen(this@Game, inject(), inject(), inject())) // Add MainMenuScreen
        }

        setScreen<LoadingScreen>()
        super.create()
    }

    override fun dispose() {
        log.debug { "Entities in engine: ${context.inject<PooledEngine>().entities.size()}" }
        context.remove<AssetManager>()
        context.dispose()
        super.dispose()
    }
}
