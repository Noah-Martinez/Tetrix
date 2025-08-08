package ch.tetrix.scoreboard.components

import ch.tetrix.scoreboard.models.ScoreDto
import ch.tetrix.scoreboard.repositories.ScoreboardRepository
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.inject.Context
import ktx.scene2d.KScrollPane
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.textButton

object ScoreboardViewBuilder {
    fun layout(context: Context, onBack: () -> Unit): KTableWidget {
        return KTableWidget(context.inject()).apply {
            setFillParent(true)

            pad(16f)
            defaults().space(16f)

            add(scoresTable(context)).colspan(3).expandX().fillX()
            row()

            textButton("MAIN MENU") {
                it.colspan(3)
                it.uniform()
                onClick { onBack() }
            }
        }
    }

    private fun scoresTable(context: Context): KTableWidget {
        val skin = context.inject<Skin>()
        val background = skin.getDrawable("table-background")
        val scoreTable = createEmptyScoreTable(context)

        return KTableWidget(context.inject()).apply {
            pad(16f)

            setBackground(background)
            defaults().align(Align.left).space(12f, 0f, 12f, 0f)
            columnDefaults(0).prefWidth(100f)
            columnDefaults(1).prefWidth(Value.percentWidth(.5f, scoreTable))
            columnDefaults(2).expandX()
            label("RANK", style = "medium")
            label("NAME", style = "medium")
            label("SCORE", style = "medium")
            row()

            scrollPane {
                setScoreTable(scoreTable)
            }.cell(colspan = 3, expand = true, fill = true)

            loadAndPopulateScores(context, scoreTable)
        }
    }

    private fun createEmptyScoreTable(context: Context): KTableWidget {
        return KTableWidget(context.inject()).apply {
            defaults().align(Align.left)
            columnDefaults(0).prefWidth(100f)
            columnDefaults(1).prefWidth(Value.percentWidth(.5f, this))
            columnDefaults(2).expandX()
        }
    }

    private fun loadAndPopulateScores(context: Context, targetTable: KTableWidget) {
        val scoreboardService = context.inject<ScoreboardRepository>()
        val scores = scoreboardService.getAllScores()

        Gdx.app.postRunnable {
            targetTable.clearChildren()

            targetTable.createScoreRows(scores)

            targetTable.invalidateHierarchy()
            targetTable.pack()

            val parent = targetTable.parent
            if (parent is KScrollPane) {
                parent.invalidateHierarchy()
                parent.layout()
            } else if (parent is com.badlogic.gdx.scenes.scene2d.ui.ScrollPane) {
                parent.invalidateHierarchy()
                parent.layout()
            }
        }
    }

    private fun KTableWidget.createScoreRows(
        scores: List<ScoreDto>,
    ) {
        defaults().align(Align.left).space(8f, 0f, 8f, 0f)
        scores.forEach { score ->
            label(score.rank.toString())
            label(score.username)
            label(score.score.toString())
            row()
        }
    }

    private fun KScrollPane.setScoreTable(scoreTable: KTableWidget) {
        setScrollbarsVisible(true)
        fadeScrollBars = false
        setOverscroll(false, false)

        addActor(scoreTable)
    }
}
