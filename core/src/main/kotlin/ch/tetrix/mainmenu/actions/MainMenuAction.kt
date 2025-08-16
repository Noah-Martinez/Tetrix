package ch.tetrix.mainmenu.actions

sealed class MainMenuAction {
    object StartGame : MainMenuAction()
    object Options : MainMenuAction()
    object Scores : MainMenuAction()
    object ExitGame : MainMenuAction()
}
