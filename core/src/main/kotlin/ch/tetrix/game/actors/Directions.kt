package ch.tetrix.game.actors

enum class Directions(val dx: Int, val dy: Int) {
    LEFT(-1, 0),
    RIGHT(1, 0),
    DOWN(0, 1),
    UP(0, -1),
}
