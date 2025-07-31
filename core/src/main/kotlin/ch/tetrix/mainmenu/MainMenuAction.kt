package ch.tetrix.mainmenu

sealed class MainMenuAction {
    object StartGame : MainMenuAction()
    object Options : MainMenuAction()
    object Scores : MainMenuAction()
    object Credits : MainMenuAction()
    object ExitGame : MainMenuAction()
}
