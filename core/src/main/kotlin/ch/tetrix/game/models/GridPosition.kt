package ch.tetrix.game.models

data class GridPosition(var x: Int, var y: Int) {
    operator fun plus(pos: GridPosition) = copy(x + pos.x, y + pos.y)
    operator fun plus(direction: Directions) = copy(x + direction.dx, y + direction.dy)

    operator fun minus(pos: GridPosition) = copy(x - pos.x, y - pos.y)
    operator fun minus(direction: Directions) = copy(x - direction.dx, y - direction.dy)

    fun rotatedClockwise() = copy(-y, x)
    fun rotatedCounterClockwise() = copy(y, -x)

    override fun toString(): String {
        return "(x:$x, y:$y)"
    }
}

