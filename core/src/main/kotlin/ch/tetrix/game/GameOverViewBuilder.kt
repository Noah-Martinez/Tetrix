package ch.tetrix.game

import ch.tetrix.scoreboard.persistence.ScoreDto
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.scene2d.textField

object GameOverViewBuilder {

    fun layout(
        skin: Skin,
        scores: List<ScoreDto>,
        stage: Stage,
        onGameOverAction: (GameOverAction) -> Unit
    ): KTableWidget {
        return KTableWidget(skin).apply {
            setFillParent(true)
            align(Align.center)

            label("GAME OVER", style = "title")
                .cell(padBottom = 40f)
            row()

            label("PLEASE ENTER YOUR NAME")
                .cell(padBottom = 20f)
            row()

            table {
                pad(4f)

                defaults().expandX().fillX().space(16f, 24f, 16f, 24f)
                // Headers
                label("RANK")
                label("NAME")
                label("SCORE")
                row()

                createScoreRows(scores, stage, onGameOverAction)

            }.cell(padBottom = 40f)
            row()

            // "MAIN MENU" Button
            textButton("MAIN MENU") {
                onClick {
                    onGameOverAction(GameOverAction.MainMenu)
                }
            }
        }
    }

    private fun KTableWidget.createScoreRows(
        scores: List<ScoreDto>,
        stage: Stage,
        onGameOverAction: (GameOverAction) -> Unit
    ) {

        scores.forEach { it }


        scores.forEach { score ->
            label(score.rank.toString())
            if (score.id == null) {
                val userInput = textField {
                    messageText = score.username
                    setTextFieldListener { textField, c ->
                        if (c == '\r' || c == '\n') {
                            onGameOverAction(GameOverAction.UsernameConfirmation(score.copy(username = textField.text)))
                        }
                    }
                }
                stage.keyboardFocus = userInput
            } else {
                label(score.username)
            }
            label(score.score.toString())
            row()

        }
    }
}
