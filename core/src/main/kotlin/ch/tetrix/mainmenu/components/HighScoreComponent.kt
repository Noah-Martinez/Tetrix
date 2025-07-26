package ch.tetrix.mainmenu.components

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.scene2d.KTableWidget
import ktx.scene2d.label

fun createHighScoreComponent(skin: Skin, score: Int): KTableWidget {
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

