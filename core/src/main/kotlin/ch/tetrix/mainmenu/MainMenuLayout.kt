package ch.tetrix.mainmenu

import ch.tetrix.mainmenu.components.createHighScoreComponent
import ch.tetrix.mainmenu.components.createMenuComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.log.Logger
import ktx.scene2d.KTableWidget

fun createMainMenuLayout(skin: Skin, log: Logger) : KTableWidget {
    val mainMenuComponent = createMenuComponent(
        skin,
        onStart = { log.info { "Start Game" } },
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
