package ch.tetrix.game.components

import ch.tetrix.game.actions.GamePauseAction
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton


object GamePauseViewBuilder {
    /**
     * Creates a single Table that acts as a full-screen pause menu.
     * This table has a semi-transparent background and centers its content.
     */
    fun layout(
        skin: Skin,
        onPauseMenuAction: (GamePauseAction) -> Unit,
        alpha: Float = 0.9f
    ): KTableWidget {
        return KTableWidget(skin).apply {
            setFillParent(true)

            center()

            val bg = skin.newDrawable("white", Color(0f, 0f, 0f, alpha))
            background = bg

            touchable = Touchable.enabled

            label("GAME PAUSE", "title-large")

            row().spaceTop(52f).spaceBottom(24f)

            table {
                defaults().width(240f).height(36f).space(16f)
                textButton("CONTINUE").onClick { onPauseMenuAction(GamePauseAction.Continue) }
                row()
                textButton("OPTIONS").onClick { onPauseMenuAction(GamePauseAction.Options) }
                row()
                textButton("MAIN MENU").onClick { onPauseMenuAction(GamePauseAction.MainMenu) }
            }
        }
    }
}
