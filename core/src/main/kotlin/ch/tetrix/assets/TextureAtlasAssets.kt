package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.assets.getAsset
import ktx.assets.load

fun AssetManager.load(asset: TextureAtlasAssets) = load<TextureAtlas>(asset.path)
operator fun AssetManager.get(asset: TextureAtlasAssets) = getAsset<TextureAtlas>(asset.path)


enum class TextureAtlasAssets(val path: String) {
//    Game("images/game.atlas")
}
