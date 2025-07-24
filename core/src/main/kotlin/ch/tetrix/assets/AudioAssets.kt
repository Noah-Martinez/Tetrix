package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import ktx.assets.getAsset
import ktx.assets.load

fun AssetManager.load(asset: AudioAssets) = load<Sound>(asset.path)
operator fun AssetManager.get(asset: AudioAssets) = getAsset<Sound>(asset.path)

enum class AudioAssets(val path: String) {
//    Drop("sounds/drop.wav")
}
