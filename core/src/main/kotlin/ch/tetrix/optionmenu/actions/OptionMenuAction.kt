package ch.tetrix.optionmenu.actions

import com.badlogic.gdx.scenes.scene2d.ui.Label

sealed class OptionMenuAction {
    object MainMenu : OptionMenuAction()
    object Reset : OptionMenuAction()
    data class KeyBindingSelected(val labelToUpdate: Label, val preferenceKey: String) : OptionMenuAction()
}
