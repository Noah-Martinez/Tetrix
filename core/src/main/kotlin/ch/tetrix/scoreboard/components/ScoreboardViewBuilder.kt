package ch.tetrix.scoreboard.components

import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.inject.Context
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton

object ScoreboardViewBuilder {
    fun layout(context: Context, onBack: () -> Unit): KTableWidget {
        return KTableWidget(context.inject()).apply {
            setFillParent(true)

            pad(16f)
            defaults().space(16f)

            add().uniform()
            add(scoresTable(context))
                .expand()
                .prefWidth(Value.percentWidth(0.5f, this))
                .minHeight(Value.percentHeight(0.5f, this@apply))
            textButton("Main Menu") {
                it.uniform()
                onClick { onBack() }
            }
        }
    }

    private fun scoresTable(context: Context): KTableWidget {
        val scoreboardService = context.inject<ScoreboardRepository>()
        val skin = context.inject<Skin>()
        val background = skin.getDrawable("table-background")

        return KTableWidget(context.inject()).apply {

            pad(16f)
            setBackground(background)

            defaults()
                .space(16f)
                .fill()
                .expandX()

            add(scoresRow(context, null, "NAME", "SCORE"))

            row()
            scrollPane {
                it.fill()
                    .expand()
                    .maxWidth(Value.percentWidth(1f, this@apply))
                setScrollbarsVisible(true)
                fadeScrollBars = false
                setOverscroll(false, false)

                table {
                    top()
                    pad(0f, 16f, 0f, 16f)

                    defaults()
                        .space(16f)
                        .fill()
                        .expandX()

                    val scores = scoreboardService.getAllScores()

                    scores.forEachIndexed { index, (_, name, score) ->
                        row()
                        add(scoresRow(context, index, name, score))
                    }
                }
            }
        }
    }

    private fun scoresRow(context: Context, index: Int?, name: String, score: Any): KTableWidget {
        return KTableWidget(context.inject()).apply {
            defaults()
                .expandX()
                .fillX()
                .center()
                .space(16f)

            label(index?.toString() ?: "") {
                it.width(Value.percentWidth(0.15f, this@apply))
                setAlignment(Align.right)
            }

            label(name) {
                it.width(Value.percentWidth(0.35f, this@apply))
                setAlignment(Align.center)
            }

            label("$score") {
                it.width(Value.percentWidth(0.35f, this@apply))
                if (index == null) {
                    setAlignment(Align.center)
                }
                else {
                    setAlignment(Align.right)
                }
            }
        }
    }
}
