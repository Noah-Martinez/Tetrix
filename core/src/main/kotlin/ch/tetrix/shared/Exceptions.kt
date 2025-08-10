package ch.tetrix.shared

import com.badlogic.gdx.Input

class ScoreboardInitException(message: String) : Exception(message)
class ScoreboardSaveException(message: String) : Exception(message)
class ScoreboardLoadException(message: String) : Exception(message)

class DuplicatedKeyBindingException(message: String, key: Int) : Exception(message) {
    val keyMapped: String = Input.Keys.toString(key)
}
