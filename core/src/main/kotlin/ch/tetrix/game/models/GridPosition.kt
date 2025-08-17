package ch.tetrix.game.models

data class GridPosition(var x: Int, var y: Int) {
    /**
     * Adds the given GridPosition to the current one by summing their respective x and y coordinates.
     *
     * @param pos The position to add to the current position.
     * @return A new GridPosition resulting from the addition of the current position and the given position.
     */
    operator fun plus(pos: GridPosition) = copy(x = x + pos.x, y = y + pos.y)
    /**
     * Adds the specified direction to the current GridPosition, effectively moving it
     * in the provided direction based on the direction's x and y offset values.
     *
     * @param direction The direction to be added, containing x and y offsets (dx, dy).
     * @return A new GridPosition resulting from the addition.
     */
    operator fun plus(direction: Directions) = copy(x = x + direction.dx, y = y + direction.dy)

    /**
     * Subtracts the given GridPosition from the current one by subtracting their respective x and y coordinates.
     *
     * @param pos The GridPosition to subtract from the current position.
     * @return A new GridPosition resulting from the subtraction of the given position from the current position.
     */
    operator fun minus(pos: GridPosition) = copy(x = x - pos.x, y = y - pos.y)
    /**
     * Subtracts the specified direction from the current GridPosition, effectively moving it
     * in the opposite direction based on the direction's x and y offset values.
     *
     * @param direction The direction to be subtracted, containing x and y offsets (dx, dy).
     * @return A new GridPosition resulting from the subtraction.
     */
    operator fun minus(direction: Directions) = copy(x = x - direction.dx, y = y - direction.dy)

    /**
     * Creates a new GridPosition by rotating the current GridPosition 90 degrees clockwise.
     *
     * The transformation swaps the x and y coordinates, negating the original y-coordinate
     * to achieve a clockwise rotation effect.
     *
     * @return A new GridPosition rotated 90 degrees clockwise relative to the current one.
     */
    fun rotatedClockwise() = copy(x = -y, y = x)
    /**
     * Creates a new GridPosition by rotating the current GridPosition 90 degrees counterclockwise.
     *
     * The transformation swaps the x and y coordinates, negating the original x-coordinate
     * to achieve a counterclockwise rotation effect.
     *
     * @return A new GridPosition rotated 90 degrees counterclockwise relative to the current one.
     */
    fun rotatedCounterClockwise() = copy(x = y, y = -x)

    override fun toString(): String {
        return "(x:$x, y:$y)"
    }
}

