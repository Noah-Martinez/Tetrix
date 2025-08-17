package ch.tetrix.game.components

import ch.tetrix.game.screens.ComponentBackground
import ch.tetrix.game.screens.ValueBackground
import ch.tetrix.game.services.GameService
import ch.tetrix.game.stages.GameStage
import ch.tetrix.shared.BehaviorSignal
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import ktx.inject.Context
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.table

object GameViewBuilder {
    fun layout(context: Context, gameStage: GameStage): KTableWidget {
        val highScoreComponent = highScoreComponent(context)
        val gameValuesComponent = gameValuesComponent(context)
        val nextShapeComponent = nextShapeContainer(context)

        return KTableWidget(context.inject()).apply {
            setFillParent(true)

            pad(16f)

            defaults().expandY().space(16f)
            table {
                top()
                it.fill()
                    .expandX()
                    .uniform()

                defaults()
                    .align(Align.right)
                    .space(16f)
                    .expandX()
                    .fillX()

                add(highScoreComponent)
            }
            add(gameStage.table).fill()
            table {
                top()
                it.fill()
                    .expandX()
                    .uniform()

                defaults()
                    .align(Align.left)
                    .space(16f)
                    .expandX()
                    .fillX()

                add(gameValuesComponent)
                row()
                add(nextShapeComponent)
            }

        }
    }

    fun highScoreComponent(context: Context): KTableWidget {
        val gameService = context.inject<GameService>()
        val tableBackground = context.inject<ComponentBackground>().drawable
        val skin = context.inject<Skin>()

        return KTableWidget(skin).apply {
            setBackground(tableBackground)
            pad(16f)
            defaults()
                .space(16f)
                .fillX()
                .expandX()

            add(valuePair(context, "HIGH SCORE", gameService.highScore))
        }
    }

    fun gameValuesComponent(context: Context): KTableWidget {
        val gameService = context.inject<GameService>()
        val tableBackground = context.inject<ComponentBackground>().drawable
        val skin = context.inject<Skin>()

        return KTableWidget(skin).apply {
            setBackground(tableBackground)
            pad(16f)
            defaults()
                .space(16f)
                .fillX()
                .expandX()

            add(valuePair(context, "SCORE", gameService.score)).row()
            add(valuePair(context, "LEVEL", gameService.level)).row()
            add(valuePair(context, "SQUARES", gameService.squares))
        }
    }

    fun nextShapeContainer(context: Context): KTableWidget {
        val tableBackground = context.inject<ComponentBackground>().drawable
        val skin = context.inject<Skin>()

        return KTableWidget(skin).apply {
            setBackground(tableBackground)
            pad(16f)

            defaults().expand()
            label("NEXT")
            row().padTop(8f).fill()
            add(nextShapeTable(context)).row()
        }
    }

    fun nextShapeTable(context: Context): KTableWidget {
        val skin = context.inject<Skin>()
        val valueBackground = context.inject<ValueBackground>().drawable

        return KTableWidget(skin).apply {
            name = "nextShapeTable"
            setBackground(valueBackground)
            top()
            left()
            defaults().uniform()

            repeat(4) {
                repeat(5) { add() }
                row()
            }
            center()
        }
    }

    private fun valuePair(context: Context, label: String, behaviorSignal: BehaviorSignal<*>): KTableWidget {
        val valueBackground = context.inject<ValueBackground>().drawable
        val skin = context.inject<Skin>()

        return KTableWidget(context.inject()).apply {
            defaults().expand()

            label(label)
            row().padTop(8f)
            table { cell ->
                cell.fill()

                setBackground(valueBackground)
                pad(8f)

                defaults().expand()

                label(behaviorSignal.value.toString()) { _ ->

                    behaviorSignal.add { _, newValue ->
                        setText(newValue.toString())
                    }

                    style = Label.LabelStyle(style).apply {
                        fontColor = skin.getColor("secondary")
                    }
                }
            }
        }
    }
}
