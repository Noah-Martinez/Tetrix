package ch.tetrix.game

import ch.tetrix.scoreboard.persistence.ScoreDto

sealed class GameOverAction {
    data class UsernameConfirmation(val score: ScoreDto) : GameOverAction()
    object MainMenu : GameOverAction()
}
