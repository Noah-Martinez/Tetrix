package ch.tetrix.game.actions

sealed class GamePauseAction {
    object Continue : GamePauseAction()
    object MainMenu : GamePauseAction()
}
