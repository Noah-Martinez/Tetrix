package ch.tetrix.game.services

import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.Shape
import ch.tetrix.game.actors.shapeImplementations.RotorShape
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import ch.tetrix.game.models.ShapeType
import ch.tetrix.game.stages.GameStage
import ch.tetrix.shared.BehaviorSignal
import ch.tetrix.shared.GameOverException
import com.badlogic.gdx.utils.Timer
import kotlin.math.max
import ktx.inject.Context
import ktx.log.logger

object GameService {
    const val NUM_COLS: Int = 17
    const val NUM_ROWS: Int = 36
    private const val INITIAL_FALL_INTERVAL_MS = 1000f
    private const val MIN_FALL_INTERVAL_MS = 100f
    private const val SQUARES_PER_LEVEL = 10
    private const val FAST_FALL_FACTOR = 5
    private const val LOCK_DELAY_S = 0.5f
    private const val MAX_LOCK_DELAY_RESETS = 7

    private val log = logger<GameService>()

    private var context: Context? = null
    private var gameStage: GameStage? = null

    /** actively controlled shape */
    private var activeShape: Shape? = null

    private var rotor: RotorShape? = null

    /** map of all cubes, to get if a position is occupied (more performant than a full array) */
    private val _cubes = arrayListOf<Cube>()
    /** map of positions -> cubes, to find occupied positions */
    val cubePositions: Map<GridPosition, Cube>
        get() = _cubes.associateBy { it.gridPos }

    val highScore = BehaviorSignal(0)
    val score = BehaviorSignal(0)
    /** level and points per cube */
    val level = BehaviorSignal(0)
    /** full squares formed around the rotor */
    val squares = BehaviorSignal(0)

    /**
     * - `true` means the game is active and playable.
     * - `false` means the game is either paused, over, or reset.
     */
    val isGameActive = BehaviorSignal(false)
    val isGamePaused = BehaviorSignal(false)
    val isGameOver = BehaviorSignal(false)

    private var fallIntervalMs = INITIAL_FALL_INTERVAL_MS
    private var isFastFallActive = false
    private var gameTimer: Timer.Task? = null
    private var lockDelay: Timer.Task? = null
    private var lockDelayResets = 0

    init {
        reset()
    }

    fun startNewGame(context: Context, gameStage: GameStage) {
        log.info { "Starting new game" }
        reset()
        this.context = context
        this.gameStage = gameStage
        setRotor(RotorShape(context))
        isGameActive.dispatch(true)
        isGameOver.dispatch(false)
        isGamePaused.dispatch(false)
        gameStep()
    }

    /**
     * Resets all game values to initial state
     */
    private fun reset() {
        context = null
        gameStage = null
        rotor = null
        activeShape = null
        isFastFallActive = false
        _cubes.clear()
        gameTimer?.cancel()
        gameTimer = null
        lockDelay?.cancel()
        lockDelay = null
        lockDelayResets = 0
        score.dispatch(0)
        level.dispatch(0)
        squares.dispatch(0)
        isGameActive.dispatch(false)
        isGamePaused.dispatch(false)
        isGameOver.dispatch(false)
        updateFallSpeed()
    }

    fun pauseGame() {
        if (isGameActive.value) {
            log.info { "Game paused" }
            isGamePaused.dispatch(true)
            isGameActive.dispatch(false)
        }
    }

    fun resumeGame() {
        if (!isGameActive.value && !isGameOver.value) {
            log.info { "Game resumed" }
            isGamePaused.dispatch(false)
            isGameActive.dispatch(true)
            scheduleNextStep()
        }
    }

    fun endGame() {
        log.info { "Game ended. Final score: ${score.value}" }
        isGameActive.dispatch(false)
        isGameOver.dispatch(true)
        isGamePaused.dispatch(false)

        if (score.value > highScore.value) {
            log.info { "New high score: ${score.value}" }
            highScore.dispatch(score.value)
        }
    }

    fun disposeAllCubes() {
        _cubes.forEach { it.dispose() }
        _cubes.clear()
    }

    fun isOutOfBounds(pos: GridPosition): Boolean {
        return pos.x < 0 || pos.x >= NUM_COLS || pos.y < 0 || pos.y >= NUM_ROWS
    }

    fun moveActiveShape(direction: Directions): MoveResult {
        if (!isGameActive.value || activeShape == null || activeShape?.fallDirection?.opposite() == direction) {
            return MoveResult.Success
        }

        val moveResult =  activeShape!!.move(direction)
        if (moveResult is MoveResult.Success) {
            handleLockDelayReset()
        }
        return moveResult
    }

    fun rotateActiveShapeClockwise() {
        if (!isGameActive.value || activeShape == null) {
            return
        }

        val success = activeShape!!.rotateClockwise()
        if (success) {
            handleLockDelayReset()
        }
    }

    fun rotateActiveShapeCounterClockwise() {
        if (!isGameActive.value || activeShape == null) {
            return
        }

        val success = activeShape!!.rotateCounterClockwise()
        if (success) {
            handleLockDelayReset()
        }
    }

    fun rotateRotorClockwise() {
        if (!isGameActive.value || rotor == null) {
            return
        }

        rotor!!.rotateClockwise()
    }

    fun rotateRotorCounterClockwise() {
        if (!isGameActive.value || rotor == null) {
            return
        }

        rotor!!.rotateCounterClockwise()
    }

    private fun gameStep() {
        val context = context
        val gameComponent = gameStage

        if (context == null || gameComponent == null) {
            throw Exception("GameComponent or context is null, game can't run without them.")
        }

        if (!isGameActive.value) {
            return
        }

        var activeShape = activeShape
        if (activeShape == null) {
            try {
                setActiveShape(ShapeType.randomShape(context))
            } catch (e: GameOverException) {
                endGame()
                return
            }
            activeShape = GameService.activeShape!!
            log.debug { "New active shape: ${activeShape.javaClass.simpleName}" }
        }

        val moveResult = activeShape.move(activeShape.fallDirection)

        if (moveResult !is MoveResult.Collision) {
            // valid step, piece is not colliding (e.g. fell into a gap)
            log.debug { "Shape moved down, reset lock delay." }
            lockDelay?.cancel()
            lockDelayResets = 0
            scheduleNextStep()
            return
        }

        handleCollision(moveResult, activeShape)
        scheduleNextStep()
    }

    private fun handleCollision(moveResult: MoveResult.Collision, activeShape: Shape) {
        val collidedWith = moveResult.collisionObject

        if (collidedWith == null) {
            handleGameBorderCollision(activeShape)
            return
        }

        // If lock delay is already running, do nothing.
        // Player moves will reset it.
        if (lockDelay?.isScheduled == true) {
            return
        }

        log.info { "Collision with ${collidedWith.javaClass.simpleName}, lock delay started." }
        lockDelayResets = 0 // Reset for the start of a new lock sequence
        startLockDelayTimer()
    }

    private fun handleGameBorderCollision(activeShape: Shape) {
        if (activeShape.fallDirection == Directions.DOWN) {
            // return to top
            activeShape.fallDirection = Directions.UP
            if (isFastFallActive) {
                disableFastFall()
            }
        }
        else if (activeShape.fallDirection == Directions.UP) {
            endGame()
        }
    }

    private fun setActiveShape(shape: Shape) {
        activeShape?.cubes?.forEach { it -> it.dispose() }
        activeShape = initShape(shape)
        lockDelay?.cancel()
        log.debug { "reset lock delay." }
        lockDelayResets = 0
    }

    private fun setRotor(shape: RotorShape) {
        rotor?.cubes?.forEach { it -> it.dispose() }
        rotor = initShape(shape)
    }

    private fun <T: Shape> initShape(shape: T): T {
        val gameComponent = gameStage
        if (gameComponent == null) {
            throw Exception("GameComponent is null, shape can't be added.")
        }

        for (cube in shape.cubes) {
            val cubePos = cube.gridPos
            if (!isOutOfBounds(cubePos)) {
                _cubes.add(cube)
            }
        }

        gameComponent.addActor(shape)
        return shape
    }

    /** used to move cubes from one shape to another (usually the rotor) */
    private fun attachActiveShapeToRotor() {
        lockDelay?.cancel()
        if (activeShape == null || rotor == null || gameStage == null) {
            return
        }

        activeShape!!.transferCubesTo(rotor!!)
        gameStage!!.actors.removeValue(activeShape, true)
        activeShape = null

        log.info { "Shape attached to rotor." }
    }

    private fun startLockDelayTimer() {
        lockDelay?.cancel()
        lockDelay = object : Timer.Task() {
            override fun run() {
                log.info { "Lock delay over, shape connected." }
                attachActiveShapeToRotor()
            }
        }
        Timer.schedule(lockDelay, LOCK_DELAY_S)
    }

    private fun handleLockDelayReset() {
        if (lockDelay?.isScheduled != true) {
            return // Not in a lock delay state
        }

        if (lockDelayResets < MAX_LOCK_DELAY_RESETS) {
            lockDelayResets++
            log.info { "Lock delay reset. Count: $lockDelayResets/$MAX_LOCK_DELAY_RESETS" }
            lockDelay?.cancel()
            startLockDelayTimer() // Restart the timer
        } else {
            log.info { "Max lock delay resets reached. Forcing shape lock." }
            lockDelay?.run()
        }
    }

    private fun updateScore(addedScore: Int) {
        score.dispatch(score.value + addedScore)
        if (highScore.value < score.value) {
            highScore.dispatch(score.value)
        }
    }

    private fun updateLevel() {
        val totalSquares = squares.value
        val currentLevel = level.value
        if (totalSquares % SQUARES_PER_LEVEL == 0 && currentLevel < totalSquares / SQUARES_PER_LEVEL) {
            log.debug { "Level up! level: $currentLevel -> ${currentLevel + 1} squares: $totalSquares" }
            level.dispatch(currentLevel + 1)
            updateFallSpeed()
        }
    }

    private fun updateFallSpeed() {
        val currentLevel = level.value
        fallIntervalMs = max(MIN_FALL_INTERVAL_MS, INITIAL_FALL_INTERVAL_MS - currentLevel * 50f)
        log.debug { "Fall speed updated to ${fallIntervalMs}ms for level $currentLevel" }

        scheduleNextStep()
    }

    private fun scheduleNextStep() {
        gameTimer?.cancel()

        if (activeShape == null || !isGameActive.value) {
            return
        }

        gameTimer = object : Timer.Task() {
            override fun run() {
                gameStep()
            }
        }

        val interval =
            if (isFastFallActive) {
                fallIntervalMs / FAST_FALL_FACTOR / 1000f
            }
            else {
                fallIntervalMs / 1000f
            }

        Timer.schedule(gameTimer, max(MIN_FALL_INTERVAL_MS / 1000f, interval))
    }

    fun enableFastFall(direction: Directions): Boolean {
        if (
            !isGameActive.value
            || activeShape == null
            || isFastFallActive
            || activeShape!!.fallDirection.opposite() == direction
        ) {
            return false
        }

        isFastFallActive = true
        log.debug { "Fast fall enabled " }
        scheduleNextStep()
        gameStep()
        return true
    }

    fun disableFastFall(): Boolean {
        if (!isFastFallActive) {
            return false
        }

        isFastFallActive = false
        log.debug { "Fast fall disabled" }
        scheduleNextStep()
        return true
    }

    fun getGameStats(): GameStats {
        return GameStats(
            score = score.value,
            level = level.value,
            squares = squares.value,
            highScore = highScore.value,
        )
    }

    data class GameStats(
        val score: Int,
        val level: Int,
        val squares: Int,
        val highScore: Int,
    )
}
