package ch.tetrix.optionmenu.components

import ch.tetrix.optionmenu.actions.OptionMenuAction
import ch.tetrix.shared.ConfigManager.playerConfig
import ch.tetrix.shared.models.TetrominoControls
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.container
import ktx.scene2d.label
import ktx.scene2d.textButton

object OptionMenuViewBuilder {
    fun layout(
        skin: Skin,
        onMenuAction: (OptionMenuAction) -> Unit
    ): KTableWidget {
        val keyBackground = skin.getDrawable("game-value-bg")

        val mainMenuComponent = optionComponent(skin, keyBackground, onMenuAction)

        return KTableWidget(skin).apply {
            setFillParent(true)
            pad(16f)
            defaults()

            row().spaceBottom(24f).expand()
            label("OPTIONS", "title")

            row().expandX()

            add(mainMenuComponent)
                .space(12f)
                .fill()
                .prefWidth(Value.percentWidth(.75f, this))
                .maxWidth(600f)

            row().expand()
            textButton("MAIN MENU").onClick { onMenuAction(OptionMenuAction.MainMenu) }
        }
    }

    fun optionComponent(
        skin: Skin,
        keyBackground: Drawable,
        onMenuAction: (OptionMenuAction) -> Unit
    ): KTableWidget {
        return KTableWidget(skin).apply {
            defaults().left().expandX().fillX()
            row().padBottom(8f)
            label("Tetromino", "large") {
                style = Label.LabelStyle(style).apply {
                    fontColor = skin.getColor("secondary")
                }
            }
            controlRow("Move Up", playerConfig.tetromino.moveUp, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_MOVE_UP))
            }
            controlRow("Move Down", playerConfig.tetromino.moveDown, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_MOVE_DOWN))
            }
            controlRow("Move Left", playerConfig.tetromino.moveLeft, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_MOVE_LEFT))
            }
            controlRow("Move Right", playerConfig.tetromino.moveRight, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_MOVE_RIGHT))
            }
            controlRow("Snap", playerConfig.tetromino.snap, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_SNAP))
            }
            controlRow("Rotate Left", playerConfig.tetromino.rotateLeft, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_ROTATE_LEFT))
            }
            controlRow("Rotate Right", playerConfig.tetromino.rotateRight, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_ROTATE_RIGHT))
            }

            row().padBottom(8f).padTop(24f)
            label("Rotor", "large") {
                style = Label.LabelStyle(style).apply {
                    fontColor = skin.getColor("secondary")
                }
            }
            controlRow("Rotate Left", playerConfig.rotor.rotateLeft, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_ROTATE_RIGHT))
            }
            controlRow("Rotate Right", playerConfig.rotor.rotateRight, keyBackground) {
                onMenuAction(OptionMenuAction.KeyBindingSelected(it, TetrominoControls.KEY_ROTATE_RIGHT))
            }

            row().pad(16f).spaceTop(8f)
            container {
                align(Align.right)
                textButton("RESET").onClick { onMenuAction(OptionMenuAction.Reset) }
            }.cell(colspan = 2)
        }
    }

    private fun KTableWidget.controlRow(
        action: String,
        key: Int,
        keyBackground: Drawable,
        onSelect: (Label) -> Unit
    ) {
        row().padTop(4f).padBottom(4f)

        label(action, "medium")

        container {
            background = keyBackground
            touchable = Touchable.enabled

            actor = label(Input.Keys.toString(key), "medium") {
                name = "key-$action"
                setAlignment(Align.center)
            }
            pad(4f, 20f, 4f, 20f)
        }.cell(
            expandX = false,
            align = Align.right,
            padRight = 20f,
        ).onClick {
            onSelect(this.findActor("key-$action"))
        }
    }
}
