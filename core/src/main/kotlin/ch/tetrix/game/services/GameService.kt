package ch.tetrix.game.services

import ch.tetrix.shared.BehaviorSignal

class GameService {
    val highScore = BehaviorSignal(0)
    val score = BehaviorSignal(0)
    val level = BehaviorSignal(0)
    val squares = BehaviorSignal(0)

    init {
        highScore.dispatch(123)
        score.dispatch(5)
        level.dispatch(0)
        squares.dispatch(2)
    }
}
