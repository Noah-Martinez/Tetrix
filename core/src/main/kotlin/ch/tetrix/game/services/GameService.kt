package ch.tetrix.game.services

import ch.tetrix.assets.AudioAssets
import ch.tetrix.assets.get
import ch.tetrix.game.actors.Cube
import ch.tetrix.game.actors.Shape
import ch.tetrix.game.actors.shapeImplementations.RotorShape
import ch.tetrix.game.models.Directions
import ch.tetrix.game.models.GridPosition
import ch.tetrix.game.models.MoveResult
import ch.tetrix.game.models.ShapeType
import ch.tetrix.game.stages.GameStage
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import ch.tetrix.shared.BehaviorSignal
import ch.tetrix.shared.ConfigManager
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Timer
import kotlin.math.max
import ktx.inject.Context
import ktx.log.logger

// TODO: visualize where shapes would end up when dropped
class GameService(private val context: Context) {
    companion object {
        private const val NUM_COLS: Int = 17
        private const val NUM_ROWS: Int = 36

        private const val INITIAL_FALL_INTERVAL_MS = 500f
        private const val MIN_FALL_INTERVAL_MS = 100f
        private const val FALL_INTERVAL_LEVE_DECREASE = 50f
        private const val SQUARES_PER_LEVEL = 5
        private const val FAST_FALL_FACTOR = 5
        private const val LOCK_DELAY_S = 0.5f
        private const val MAX_LOCK_DELAY_RESETS = 7

        private val log = logger<GameService>()
    }

    val cols: Int
        get() = NUM_COLS

    val rows: Int
        get() = NUM_ROWS

    private lateinit var gameStage: GameStage
    private var assets: AssetManager = context.inject<AssetManager>()
    private val config by lazy { ConfigManager.playerConfig }

    private lateinit var tetrominoRotateSound: Sound
    private lateinit var tetrominoMoveSound: Sound
    private lateinit var tetrominoLandedSound: Sound
    private lateinit var tetrominoHardDropSound: Sound
    private lateinit var tetrominoFallingAfterSquareClearSound: Sound
    private lateinit var squareClearSound: Sound
    private lateinit var rotorRotateSound: Sound
    private lateinit var levelUpSound: Sound
    private lateinit var gameOverSound: Sound

    /** actively controlled shape */
    private var activeShape: Shape? = null
    private lateinit var rotor: RotorShape

    private val _shapes = arrayListOf<Shape>()
    /** map of positions -> cubes, to find occupied positions */
    val cubePositions: Map<GridPosition, Cube>
        get() = _shapes.flatMap { it.cubes.toList() }.associateBy { it.gridPos }

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
        setGamesound()
    }


    fun startNewGame(gameStage: GameStage) {
        log.info { "Starting new game" }
        reset()
        this.gameStage = gameStage
        val scoreboardRepo = context.inject<ScoreboardRepository>()
        highScore.dispatch(scoreboardRepo.getHighScore()?.score ?: 0)
        setRotor(RotorShape(context, squareClearSound, tetrominoFallingAfterSquareClearSound))
        isGameActive.dispatch(true)
        isGameOver.dispatch(false)
        isGamePaused.dispatch(false)
        gameStep()
    }

    private fun setGamesound() {
        tetrominoRotateSound = assets[AudioAssets.TETROMINO_ROTATE]
        tetrominoMoveSound = assets[AudioAssets.TETROMINO_MOVE]
        tetrominoLandedSound = assets[AudioAssets.TETROMINO_LANDED]
        tetrominoHardDropSound = assets[AudioAssets.TETROMINO_HARD_DROP]
        tetrominoFallingAfterSquareClearSound = assets[AudioAssets.TETROMINO_FALLING_AFTER_SQUARE_CLEAR]
        squareClearSound = assets[AudioAssets.SQUARE_CLEAR]
        rotorRotateSound = assets[AudioAssets.ROTOR_ROTATE]
        levelUpSound = assets[AudioAssets.LEVEL_UP]
        gameOverSound = assets[AudioAssets.GAME_OVER]
    }

    /**
     * Resets all game values to initial state
     */
    private fun reset() {
        if(::gameStage.isInitialized) {
            gameStage.dispose()
        }
        if(::rotor.isInitialized) {
            rotor.remove()
        }
        activeShape?.remove()

        isFastFallActive = false
        _shapes.clear()
        gameTimer?.cancel()
        gameTimer = null
        lockDelay?.cancel()
        lockDelay = null
        lockDelayResets = 0
        score.dispatch(0)
        level.dispatch(0)
        squares.dispatch(0)
        highScore.dispatch(0)
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
        gameOverSound.play(config.audio.soundVolume)
        isGameActive.dispatch(false)
        isGameOver.dispatch(true)
        isGamePaused.dispatch(false)

        if (score.value > highScore.value) {
            log.info { "New high score: ${score.value}" }
            highScore.dispatch(score.value)
        }
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

    fun snapActiveShape() {
        val lockDelay = this.lockDelay
        val activeShape = this.activeShape
        if (!isGameActive.value || activeShape == null || (lockDelay != null && lockDelay.isScheduled)) {
            return
        }

        gameTimer?.cancel()
        var result: MoveResult?

        do {
            result = moveActiveShape(activeShape.fallDirection)
        } while (result is MoveResult.Success)

        tetrominoHardDropSound.play(config.audio.soundVolume)
        handleCollision(result as MoveResult.Collision, activeShape)
    }

    fun rotateActiveShapeClockwise() {
        if (!isGameActive.value || activeShape == null) {
            return
        }

        val success = activeShape!!.rotateClockwise()
        if (success) {
            tetrominoRotateSound.play(config.audio.soundVolume)
            handleLockDelayReset()
        }
    }

    fun rotateActiveShapeCounterClockwise() {
        if (!isGameActive.value || activeShape == null) {
            return
        }

        val success = activeShape!!.rotateCounterClockwise()
        if (success) {
            tetrominoRotateSound.play(config.audio.soundVolume)
            handleLockDelayReset()
        }
    }

    fun rotateRotorClockwise() {
        if (!isGameActive.value) {
            return
        }

        rotorRotateSound.play(config.audio.soundVolume)
        rotor.rotateClockwise()
    }

    fun rotateRotorCounterClockwise() {
        if (!isGameActive.value) {
            return
        }

        rotorRotateSound.play(config.audio.soundVolume)
        rotor.rotateCounterClockwise()
    }

    private fun gameStep() {
        if (!isGameActive.value) {
            return
        }

        var activeShape = activeShape
        if (activeShape == null) {
            setActiveShape(ShapeType.randomShape(context))
            activeShape = this.activeShape!!
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
    }

    private fun handleCollision(moveResult: MoveResult.Collision, activeShape: Shape) {
        val collidedWith = moveResult.collisionObject

        scheduleNextStep()

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
        activeShape = addShape(shape)
        lockDelay?.cancel()
        log.debug { "reset lock delay." }
        lockDelayResets = 0
    }

    private fun setRotor(shape: RotorShape) {
        rotor = addShape(shape)
    }

    private fun <T: Shape> addShape(shape: T): T{
        val gameComponent = gameStage

        val canPlaceShape = shape.cubes.map { it.gridPos }.all { !cubePositions.containsKey(it) }

        if (!canPlaceShape) {
            log.info { "Unable to place shape ${shape.javaClass.simpleName}, it collides with existing cubes." }
            endGame()
        }

        _shapes.add(shape)

        gameComponent.addActor(shape)
        return shape
    }

    /** used to move cubes from one shape to another (usually the rotor) */
    private fun attachActiveShapeToRotor() {
        lockDelay?.cancel()
        val activeShape = activeShape

        if (activeShape == null || activeShape.canMove()) {
            return
        }

        activeShape.transferCubesTo(rotor)
        tetrominoLandedSound.play(config.audio.soundVolume)
        activeShape.remove()
        _shapes.remove(activeShape)
        this.activeShape = null

        gameTimer?.cancel()

        rotor.removeSquaresAsync { result ->
            log.info { "Shape attached to rotor. Cubes: ${result.cubesDestroyed}, Squares: ${result.squaresDestroyed}" }
            updateScore(result.cubesDestroyed * result.squaresDestroyed)
            squares.dispatch(squares.value + result.squaresDestroyed)
            updateLevel()

            log.error { "Next step" }
            scheduleNextStep()
        }
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
        val actualLevel = Math.floorDiv(totalSquares, SQUARES_PER_LEVEL)

        if (currentLevel != actualLevel) {
            levelUpSound.play(config.audio.soundVolume)
            level.dispatch(actualLevel)
            log.info { "Level up! level: $currentLevel -> $actualLevel squares: $totalSquares" }
            updateFallSpeed()
        }
    }

    private fun updateFallSpeed() {
        val currentLevel = level.value
        fallIntervalMs = max(MIN_FALL_INTERVAL_MS, INITIAL_FALL_INTERVAL_MS - currentLevel * FALL_INTERVAL_LEVE_DECREASE)
        log.info { "Fall speed updated to ${fallIntervalMs}ms for level $currentLevel" }

        scheduleNextStep()
    }

    private fun scheduleNextStep() {
        gameTimer?.cancel()

        if (!isGameActive.value) {
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
}
