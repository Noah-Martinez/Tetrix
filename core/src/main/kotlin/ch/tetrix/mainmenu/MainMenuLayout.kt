package ch.tetrix.mainmenu

import ch.tetrix.Game
import ch.tetrix.game.GameScreen
import ch.tetrix.mainmenu.components.createHighScoreComponent
import ch.tetrix.mainmenu.components.createMenuComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.inject.Context
import ktx.log.Logger
import ktx.scene2d.KTableWidget

fun createMainMenuLayout(skin: Skin, log: Logger, context: Context) : KTableWidget {
    val game: Game = context.inject()

    val mainMenuComponent = createMenuComponent(
        skin,
        onStart = {
            game.removeScreen<MainMenuScreen>()
            game.addScreen(GameScreen(context))
            game.setScreen<GameScreen>()
        },
        onOptions = { log.info { "Options" } },
        onScores = { log.info { "Scores" } },
        onCredits = { log.info { "Credits" } },
        onExit = { Gdx.app.exit() }
    )

    val highScoreComponent = createHighScoreComponent(skin, 123456789)

    return KTableWidget(skin).apply {
        setFillParent(true)
        row().pad(12f)
        add(highScoreComponent)
        row()
        add(mainMenuComponent).pad(12f).expand()
    }
}
