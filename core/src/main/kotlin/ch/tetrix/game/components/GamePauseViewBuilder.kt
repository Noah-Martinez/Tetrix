package ch.tetrix.game.components

import ch.tetrix.game.actions.GamePauseAction
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

object GamePauseViewBuilder {
    fun layout(
        skin: Skin,
        stage: Stage,
        onPauseMenuAction: (GamePauseAction) -> Unit,
        alpha: Float = 0.9f
    ): Group {
        return object : Group() {
            private val pauseMenu = createPauseMenu(skin, onPauseMenuAction)

            init {
                addActor(pauseMenu)

                pauseMenu.pack()
                pauseMenu.setPosition(
                    (stage.width - pauseMenu.width) / 2,
                    (stage.height - pauseMenu.height) / 2
                )
            }

            override fun draw(batch: Batch, parentAlpha: Float) {
                val color = Color(0f, 0f, 0f, alpha * parentAlpha)
                batch.color = color
                batch.draw(
                    skin.getRegion("white"),
                    0f, 0f, stage.width, stage.height
                )
                batch.color = Color.WHITE

                super.draw(batch, parentAlpha)
            }

            override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
                val child = super.hit(x, y, touchable)
                return child ?: this
            }
        }
    }

    private fun createPauseMenu(
        skin: Skin,
        onPauseMenuAction: (GamePauseAction) -> Unit
    ): KTableWidget {
        return KTableWidget(skin).apply {
            label("GAME PAUSE", "title-large")
            row().space(52f, 0f, 24f, 0f)
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
