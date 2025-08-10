package ch.tetrix.game.actors.shapeImplementations

import ch.tetrix.assets.TextureAssets
import ch.tetrix.assets.get
import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.Shape
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import ch.tetrix.game.services.GameService
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context
import kotlin.math.abs

class RotorShape(
    context: Context,
) : Shape(
    GridPosition(8, 27),
    arrayOf(
        GridPosition(0, 0),
    ),
    context,
    texture = context.inject<AssetManager>()[TextureAssets.CUBE_RED]
) {
    data class SquaresResult(
        val cubesDestroyed: Int,
        val squaresDestroyed: Int,
    )

    val maxCubeRadius
        get() = cubes.flatMap { listOf(abs(it.localPos.x), abs(it.localPos.y)) }.maxOrNull() ?: 0

    val cubePositions
        get() = cubes.map { it.localPos }.toSet()

    override fun move(direction: Directions): MoveResult {
        return MoveResult.Success
    }

    /**
     * Detects and removes full squares.
     * Moves in outer cubes after removing squares and does this until no squares are left on the rotor.
     *
     * @return cubesDestroyed & squaresDestroyed
     */
    fun removeSquares(): SquaresResult {
        var cubesDestroyed = 0
        var fullSquares = 0
        var radius = 1

        var consecutiveSquares = 0

        while (radius <= maxCubeRadius) {
            log.info { "Checking for full squares in radius $radius" }
            val perimeterPositions = getPerimeterPositions(radius)

            val isPerimeterFull = perimeterPositions.all { cubePositions.contains(it) }

            if (isPerimeterFull) {
                log.info { "Found full square of ${perimeterPositions.size} in radius $radius" }
                val perimeterCubes = perimeterPositions.mapNotNull { cubes.find { cube -> cube.localPos == it } }

                perimeterCubes.forEach {
                    _cubes.remove(it)
                    it.remove()
                    cubesDestroyed++
                }
                fullSquares++
                consecutiveSquares++
            }
            else if (consecutiveSquares > 0) {
                moveCubesIn(radius, consecutiveSquares)
                radius -= consecutiveSquares
                consecutiveSquares = 0
                continue
            }

            radius++
        }

        return SquaresResult(cubesDestroyed, fullSquares)
    }

    private fun getPerimeterPositions(radius: Int): Set<GridPosition> {
        val perimeterPositions = mutableSetOf<GridPosition>()
        if (radius < 1) return setOf()

        // top row (excluding top-left corner)
        for (x in (-radius + 1) .. radius) {
            perimeterPositions.add(GridPosition(x, -radius))
        }
        // right column (excluding bottom-right corner)
        for (y in (-radius + 1) .. radius) {
            perimeterPositions.add(GridPosition(radius, y))
        }
        // bottom row (excluding bottom-left corner)
        for (x in radius - 1 downTo -radius) {
            perimeterPositions.add(GridPosition(x, radius))
        }
        // left column
        for (y in radius - 1 downTo -radius) {
            perimeterPositions.add(GridPosition(-radius, y))
        }

        return perimeterPositions
    }

    private fun moveCubesIn(radius: Int, stepCount: Int) {
        log.info { "Starting to move in cubes from radius $radius" }
        var moveRadius = radius

        // move outward to move cubes inward
        while (moveRadius <= maxCubeRadius) {
            val cubesToMove = cubes.filter {
                abs(it.localPos.x) == moveRadius || abs(it.localPos.y) == moveRadius
            }

            log.info { "Moving in ${cubesToMove.size} cubes in radius $moveRadius for $stepCount steps" }
            if (cubesToMove.isEmpty()) {
                moveRadius++
                continue
            }

            val sortedCubes = sortCubesClockwise(cubesToMove, moveRadius)

            sortedCubes.forEach { cube ->
                for (step in 0 until stepCount) {
                    val nextStepPos = cube.localPos.moveInward()
                    if (nextStepPos == cube.localPos) {
                        // something blocking the way or can't move further
                        break
                    }
                    cube.localPos = nextStepPos
                }
            }

            moveRadius++
        }
    }

    /**
     * Sorts a list of cubes that are on a specific perimeter in clockwise order.
     * The sorting starts at the top side, from left to right, but places the
     * top-left corner cube at the very end of the list.
     *
     * @param cubesOnPerimeter The list of cubes to be sorted. It is assumed that all
     * cubes in this list are on the same perimeter.
     * @param radius The radius of the perimeter.
     * @return A new list of cubes sorted in a clockwise fashion, with any corners cubes first
     */
    private fun sortCubesClockwise(cubesOnPerimeter: List<Cube>, radius: Int): List<Cube> {
        return cubesOnPerimeter.sortedWith(Comparator { c1, c2 ->
            val pos1 = c1.localPos
            val pos2 = c2.localPos

            fun getSidePriority(pos: GridPosition): Int {
                return when {
                    abs(pos.x) == radius && abs(pos.y) == radius -> 0 // corner cubes first
                    pos.y == -radius -> 1 // Top side
                    pos.x == radius -> 2  // Right side
                    pos.y == radius -> 3  // Bottom side
                    pos.x == -radius -> 4 // Left side
                    else -> -1 // Should not happen for perimeter cubes
                }
            }

            val side1 = getSidePriority(pos1)
            val side2 = getSidePriority(pos2)

            // If cubes are on different sides, the one with the lower priority comes first.
            if (side1 != side2) {
                return@Comparator side1.compareTo(side2)
            }

            return@Comparator when (side1) {
                0 -> pos1.x.compareTo(pos2.x)       // Top: sort by increasing x
                1 -> pos1.y.compareTo(pos2.y)       // Right: sort by increasing y
                2 -> pos2.x.compareTo(pos1.x)       // Bottom: sort by decreasing x
                3 -> pos2.y.compareTo(pos1.y)       // Left: sort by decreasing y
                else -> 0
            }
        })
    }

    /**
     * Helper function to determine the new inward position of a cube.
     */
    private fun GridPosition.moveInward(): GridPosition {
        val radius = maxOf(abs(this.x), abs(this.y))

        // If the position is already at the center, it can't move further inward.
        if (radius < 1) {
            return this
        }

        val newPosition = when {
            // Top side
            this.y == -radius && abs(this.x) < radius -> GridPosition(this.x, this.y + 1)
            // Bottom side
            this.y == radius && abs(this.x) < radius -> GridPosition(this.x, this.y - 1)
            // Left side
            this.x == -radius && abs(this.y) < radius -> GridPosition(this.x + 1, this.y)
            // Right side
            this.x == radius && abs(this.y) < radius -> GridPosition(this.x - 1, this.y)

            // Corners: The movement of corners is a bit tricky.
            // They move diagonally inwardly.
            this.x == radius && this.y == -radius -> GridPosition(this.x - 1, this.y + 1) // Top-right
            this.x == radius && this.y == radius -> GridPosition(this.x - 1, this.y - 1) // Bottom-right
            this.x == -radius && this.y == radius -> GridPosition(this.x + 1, this.y - 1) // Bottom-left
            this.x == -radius && this.y == -radius -> GridPosition(this.x + 1, this.y + 1) // Top-left

            else -> this // Should not happen
        }

        if (cubePositions.contains(newPosition) || GameService.isOutOfBounds(newPosition + gridPos)) {
            // cannot move to occupied space
            return this
        }

        return newPosition
    }
}
