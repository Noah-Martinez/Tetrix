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

    FontAssets.entries.forEach { load(it) }
    finishLoading()

    val resourcesMap = ObjectMap<String, Any>().apply {
        FontAssets.entries.forEach {
            put(it.skinName, get(it))
        }
    }

    val skinParams = SkinLoader.SkinParameter(asset.atlasPath, resourcesMap)

    return load(asset.jsonPath, skinParams)
}

enum class SkinAssets(val jsonPath: String, val atlasPath: String) {
    Default ("ui/uiskin.json", "ui/uiskin.atlas")
}
