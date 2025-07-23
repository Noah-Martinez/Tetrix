package ch.tetrix

import ch.tetrix.assets.*
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ObjectMap
import ktx.assets.Asset
import ktx.assets.getAsset
import ktx.assets.load
import ktx.assets.loadAsset

fun AssetManager.load(asset: SoundAssets) = load<Sound>(asset.path)
operator fun AssetManager.get(asset: SoundAssets) = getAsset<Sound>(asset.path)

fun AssetManager.load(asset: MusicAssets) = load<Music>(asset.path)
operator fun AssetManager.get(asset: MusicAssets) = getAsset<Music>(asset.path)

fun AssetManager.load(asset: TextureAtlasAssets) = load<TextureAtlas>(asset.path)
operator fun AssetManager.get(asset: TextureAtlasAssets) = getAsset<TextureAtlas>(asset.path)

// NOTE: had to use a custom font key, in case the same font file with different configs is used
// (freeTypeFontLoader uses filename as the default asset name)
private fun getFontKey(asset: FontAssets) = "${asset.path}-${asset.name}"
fun AssetManager.load(asset: FontAssets): Asset<BitmapFont> {
    val params = FreetypeFontLoader.FreeTypeFontLoaderParameter().apply {
        fontFileName = asset.path
        asset.params(this.fontParameters)
    }

    val descriptor = AssetDescriptor(
        getFontKey(asset),
        BitmapFont::class.java,
        params
    )

    return loadAsset(descriptor)
}
operator fun AssetManager.get(asset: FontAssets): BitmapFont = getAsset<BitmapFont>(getFontKey(asset))

fun AssetManager.load(asset: SkinAssets): Asset<Skin> {
    // load in the default font for the skin

    // TODO: not optimal but works for now
    load(FontAssets.Default)
    load(FontAssets.Title)
    finishLoading()

    val resourcesMap = ObjectMap<String, Any>().apply {
        put("default", get(FontAssets.Default))
        put("font", get(FontAssets.Default))
        put("list", get(FontAssets.Default))
        put("subtitle", get(FontAssets.Default))
        put("window", get(FontAssets.Default))
        put("title", get(FontAssets.Title))
    }

    val skinParams = SkinLoader.SkinParameter(asset.atlasPath, resourcesMap)

    return load(asset.jsonPath, skinParams)
}
operator fun AssetManager.get(asset: SkinAssets): Skin = getAsset(asset.jsonPath)
