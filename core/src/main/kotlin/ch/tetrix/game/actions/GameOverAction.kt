package ch.tetrix.game.actions

import ch.tetrix.scoreboard.models.ScoreDto

sealed class GameOverAction {
    data class UsernameConfirmation(val score: ScoreDto) : GameOverAction()
    object MainMenu : GameOverAction()
}
