package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import ktx.assets.Asset
import ktx.assets.getAsset
import ktx.assets.loadAsset

operator fun AssetManager.get(asset: FontAssets): BitmapFont = getAsset<BitmapFont>(asset.skinName)

// NOTE: had to use a custom font key, in case the same font file with different configs is used
// (freeTypeFontLoader uses filename as the default asset name)
fun AssetManager.load(asset: FontAssets): Asset<BitmapFont> {
    val params = FreetypeFontLoader.FreeTypeFontLoaderParameter().apply {
        fontFileName = asset.path
        asset.params(this.fontParameters)
    }

    val descriptor = AssetDescriptor(
        asset.skinName,
        BitmapFont::class.java,
        params
    )

    return loadAsset(descriptor)
}

enum class FontAssets(
    /** name to be loaded into the skin (must be unique) */
    val skinName: String,
    val path: String,
    val params: FreeTypeFontParameter.() -> Unit,
) {
    Default(
        "press-start-2p",
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 12
            mono = true
            kerning = true
            incremental = true
            padTop = 2
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.Nearest
        }
    ),
    Medium(
        "medium",
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 16
            mono = true
            kerning = true
            incremental = true
            padTop = 4
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.Nearest
        }
    ),
    Large(
        "large",
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 20
            mono = true
            kerning = true
            incremental = true
            padTop = 4
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.Nearest
        }
    ),
    Title(
        "title",
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 28
            mono = true
            kerning = true
            incremental = true
            padTop = 4
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.Nearest
        }
    ),
    TitleLarge(
        "title-large",
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 48
            mono = true
            kerning = true
            incremental = true
            padTop = 4
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.Nearest
            spaceX = 12
            shadowColor = Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.08f)
            shadowOffsetX = 5
            shadowOffsetY = 5
        }
    )
}
