package ch.tetrix.optionmenu.actions

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Slider

sealed class OptionMenuAction {
    object MainMenu : OptionMenuAction()
    object Reset : OptionMenuAction()
    data class KeyBindingSelected(val label: Label, val preferenceKey: String) : OptionMenuAction()
    data class AudioVolumeChanged(val slider: Slider, val preferenceKey: String) : OptionMenuAction()
}
