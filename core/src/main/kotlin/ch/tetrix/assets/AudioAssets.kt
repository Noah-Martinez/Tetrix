package ch.tetrix.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import ktx.assets.getAsset
import ktx.assets.load

fun AssetManager.loadSound(asset: AudioAssets) = load<Sound>(asset.path)
operator fun AssetManager.get(asset: AudioAssets) = getAsset<Sound>(asset.path)

enum class AudioAssets(val path: String) {
    GAME_OVER("sounds/game_over.wav"),
    LEVEL_UP("sounds/level_up.wav"),
    ROTOR_ROTATE("sounds/rotor_rotate.wav"),
    SQUARE_CLEAR("sounds/square_clear.wav"),
    TETROMINO_FALLING_AFTER_SQUARE_CLEAR("sounds/tetromino_falling_after_square_clear.wav"),
    TETROMINO_HARD_DROP("sounds/tetromino_hard_drop.wav"),
    TETROMINO_LANDED("sounds/tetromino_landed.wav"),
    TETROMINO_MOVE("sounds/tetromino_move.wav"),
    TETROMINO_ROTATE("sounds/tetromino_rotate.wav"),
}

fun AssetManager.loadMusic(asset: MusicAssets) = load<Music>(asset.path)
operator fun AssetManager.get(asset: MusicAssets) = getAsset<Music>(asset.path)

enum class MusicAssets(val path: String) {
    MAIN_MENU("sounds/main_menu.mp3")
}

