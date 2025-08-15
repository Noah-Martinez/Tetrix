package ch.tetrix.mainmenu.components

import ch.tetrix.mainmenu.actions.MainMenuAction
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

object MainMenuViewBuilder {
    fun layout(
        skin: Skin,
        onMenuAction: (MainMenuAction) -> Unit,
        highScore: Int
    ) : KTableWidget {
        val mainMenuComponent = menuComponent(skin, onMenuAction)

        val highScoreComponent = highScoreComponent(skin, highScore)

        return KTableWidget(skin).apply {
            setFillParent(true)
            row().pad(12f)
            add(highScoreComponent)
            row()
            add(mainMenuComponent).pad(12f).expand()
        }
    }


    fun highScoreComponent(skin: Skin, score: Int): KTableWidget {
        return KTableWidget(skin).apply {
            top()
            label("HIGH SCORE") {
                it.padBottom(4f)
            }
            row()
            label("$score") {
                it.padBottom(20f)
            }
        }
    }

    fun menuComponent(
        skin: Skin,
        onMenuAction: (MainMenuAction) -> Unit
    ) : KTableWidget {
        return KTableWidget(skin).apply {
            row().expand()
            label("TETRIX", "title-large")
            row().width(320f).height(48f).space(52f, 0f, 24f, 0f)
            textButton("START GAME", "large").apply {
                onClick { onMenuAction(MainMenuAction.StartGame) }
            }
            row()
            table {
                defaults().width(250f).height(32f).pad(8f)
                textButton("OPTIONS").onClick { onMenuAction(MainMenuAction.Options) }
                row()
                textButton("SCORES").onClick { onMenuAction(MainMenuAction.Scores) }
                row()
                textButton("CREDITS").onClick { onMenuAction(MainMenuAction.Credits) }
                row()
                textButton("EXIT").onClick { onMenuAction(MainMenuAction.ExitGame) }
            }
        }
    }
}
