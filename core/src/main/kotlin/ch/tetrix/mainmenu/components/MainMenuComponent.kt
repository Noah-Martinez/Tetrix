package ch.tetrix.mainmenu.components

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

fun createMenuComponent(
    skin: Skin,
    onStart: () -> Unit,
    onOptions: () -> Unit,
    onScores: () -> Unit,
    onCredits: () -> Unit,
    onExit: () -> Unit
) : KTableWidget {
    return KTableWidget(skin).apply {
        row().expand().pad(20f)
        label("TETRIX").setFontScale(5f)
        row().width(300f).height(52f).pad(12f)
        textButton("START GAME").apply {
            label.setFontScale(1.5f)
            onClick { onStart() }
        }
        row()
        table {
            defaults().width(250f).height(32f).pad(8f)
            textButton("OPTIONS").onClick { onOptions() }
            row()
            textButton("SCORES").onClick { onScores() }
            row()
            textButton("CREDITS").onClick { onCredits() }
            row()
            textButton("EXIT").onClick { onExit() }
        }
    }
}
