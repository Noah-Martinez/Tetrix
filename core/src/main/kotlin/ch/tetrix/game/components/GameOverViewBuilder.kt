package ch.tetrix.game.components

import ch.tetrix.game.actions.GameOverAction
import ch.tetrix.scoreboard.models.ScoreDto
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.*

object GameOverViewBuilder {
    fun layout(
        skin: Skin,
        userScore: Int,
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

                createScoreRows(skin, scores, stage, onGameOverAction)

            }.cell(padBottom = 40f)
            row()

            // "MAIN MENU" Button
            textButton("MAIN MENU") {
                name = "game-over-main-menu-btn"
                pad(8f)
                onClick {
                    val username = stage.root.findActor<TextField>("score-input").text
                    val score = ScoreDto(username = username, score = userScore)
                    onGameOverAction(GameOverAction.UsernameConfirmation(score))
                }
            }
        }
    }

    private fun KTableWidget.createScoreRows(
        skin: Skin,
        scores: List<ScoreDto>,
        stage: Stage,
        onGameOverAction: (GameOverAction) -> Unit
    ) {
        scores.forEach { score ->
            label(score.rank.toString())
            if (score.id == null) {
                container {
                    background = skin.getDrawable("game-value-bg")
                    val userInput = textField("", "nobg") {
                        name = "score-input"
                        messageText = score.username
                        setTextFieldListener { textField, c ->
                            if (c == '\r' || c == '\n') {
                                onGameOverAction(GameOverAction.UsernameConfirmation(score.copy(username = textField.text)))
                            }
                        }
                    }
                    stage.keyboardFocus = userInput
                }
            } else {
                label(score.username)
            }
            label(score.score.toString())
            row()

        }
    }
}
