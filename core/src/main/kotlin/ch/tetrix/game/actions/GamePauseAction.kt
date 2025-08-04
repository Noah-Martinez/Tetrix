package ch.tetrix.game.actions

sealed class GamePauseAction {
    object Continue : GamePauseAction()
    object Options : GamePauseAction()
    object MainMenu : GamePauseAction()
}
