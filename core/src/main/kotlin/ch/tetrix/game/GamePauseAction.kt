package ch.tetrix.game

sealed class GamePauseAction {
    object Continue : GamePauseAction()
    object Options : GamePauseAction()
    object MainMenu : GamePauseAction()
}
