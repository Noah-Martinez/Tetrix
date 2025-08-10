package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.graphics.Texture
import ktx.assets.getAsset

operator fun AssetManager.get(asset: TextureAssets) = getAsset<Texture>(asset.path)

fun AssetManager.loadClamped(asset: TextureAssets) {
    val p = TextureLoader.TextureParameter().apply {
        genMipMaps = true
        minFilter = Texture.TextureFilter.Nearest
        magFilter = Texture.TextureFilter.Nearest
        wrapU = Texture.TextureWrap.ClampToEdge
        wrapV = Texture.TextureWrap.ClampToEdge
    }
    load(asset.path, Texture::class.java, p)
}

enum class TextureAssets(val path: String) {
    CUBE_RED("textures/cube_red.png"),
    CUBE_GREEN("textures/cube_green.png"),
    CUBE_YELLOW("textures/cube_yellow.png"),
    CUBE_PURPLE("textures/cube_purple.png"),
    CUBE_ORANGE("textures/cube_orange.png"),
    CUBE_BLUE("textures/cube_blue.png"),
    CUBE_MAGENTA("textures/cube_magenta.png"),
    CUBE_CYAN("textures/cube_cyan.png"),
}
