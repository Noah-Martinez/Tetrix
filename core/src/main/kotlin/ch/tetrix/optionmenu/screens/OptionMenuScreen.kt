package ch.tetrix.optionmenu.screens

import ch.tetrix.GAME_HEIGHT
import ch.tetrix.GAME_WIDTH
import ch.tetrix.Game
import ch.tetrix.mainmenu.screens.MainMenuScreen
import ch.tetrix.optionmenu.actions.OptionMenuAction
import ch.tetrix.optionmenu.components.OptionMenuViewBuilder
import ch.tetrix.shared.ConfigManager
import ch.tetrix.shared.ConfigManager.resetConfig
import ch.tetrix.shared.DuplicatedKeyBindingException
import ch.tetrix.shared.Snackbar
import ch.tetrix.shared.SnackbarPosition
import ch.tetrix.shared.TxScreen
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.inject.Context
import ktx.log.logger
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin

class OptionMenuScreen(context: Context) : TxScreen() {
    private val batch by lazy { context.inject<Batch>() }
    private val game by lazy { context.inject<Game>() }
    private val inputMultiplexer by lazy { context.inject<InputMultiplexer>() }

    private val viewport by lazy { ExtendViewport(GAME_WIDTH, GAME_HEIGHT) }
    override val stage by lazy { Stage(viewport, batch) }

    private var optionMenuLayout = createOptionMenuLayout(Scene2DSkin.defaultSkin)

    private var labelToUpdate: Label? = null

    companion object {
        private val log = logger<OptionMenuScreen>()
    }

    override fun show() {
        super.show()
        inputMultiplexer.addProcessor(stage)
        stage.addActor(optionMenuLayout)
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(stage)
    }

    private fun createOptionMenuLayout(skin: Skin): KTableWidget {
        return OptionMenuViewBuilder.layout(
            skin = skin,
            onMenuAction = ::handleMenuAction
        )
    }

    private fun handleMenuAction(action: OptionMenuAction) {
        when (action) {
            is OptionMenuAction.MainMenu -> goToMainMenu()
            is OptionMenuAction.Reset -> onResetConfig()
            is OptionMenuAction.KeyBindingSelected -> startKeyRebinding(action.label, action.preferenceKey)
            is OptionMenuAction.AudioVolumeChanged -> startAudioChange(action.slider, action.preferenceKey)
        }
    }

    private fun onResetConfig() {
        resetConfig()
        stage.clear() // Clear all actors from the stage
        optionMenuLayout = createOptionMenuLayout(Scene2DSkin.defaultSkin) // Recreate the layout
        stage.addActor(optionMenuLayout) // Add the new layout
    }

    private fun startKeyRebinding(label: Label, preferenceKey: String) {
        labelToUpdate = label

        label.apply {
            setText("...")
            style = Label.LabelStyle(style).apply {
                fontColor = skin.getColor("secondary")
            }
        }

        inputMultiplexer.removeProcessor(stage)

        inputMultiplexer.addProcessor(object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                if (labelToUpdate != null) {
                    try {
                        ConfigManager.keyBindingChanged(preferenceKey, keycode)
                    } catch (ex: DuplicatedKeyBindingException) {
                        Snackbar.show(
                            skin = skin,
                            stage = stage,
                            message = "Key ${ex.keyMapped} already bound to another action",
                            position = SnackbarPosition.BOTTOM_RIGHT
                        )
                        return false
                    }

                    labelToUpdate?.let {
                        it.setText(Input.Keys.toString(keycode))
                        it.style = Label.LabelStyle(it.style).apply {
                            fontColor = skin.getColor("neutral-variant")
                        }
                    }

                    labelToUpdate = null
                }

                inputMultiplexer.removeProcessor(this)
                inputMultiplexer.addProcessor(stage)
                return true
            }
        })
    }

    private fun startAudioChange(slider: Slider, preferenceKey: String) {
        ConfigManager.audioChanged(preferenceKey, slider.value)
    }


        private fun goToMainMenu() {
        game.removeScreen<OptionMenuScreen>()
        game.setScreen<MainMenuScreen>()
    }
}
