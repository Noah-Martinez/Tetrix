package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ObjectMap
import ktx.assets.Asset
import ktx.assets.getAsset
import ktx.assets.load

operator fun AssetManager.get(asset: SkinAssets): Skin = getAsset(asset.jsonPath)

fun AssetManager.load(asset: SkinAssets): Asset<Skin> {

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

enum class SkinAssets(val jsonPath: String, val atlasPath: String) {
    Default ("ui/uiskin.json", "ui/uiskin.atlas")
}
