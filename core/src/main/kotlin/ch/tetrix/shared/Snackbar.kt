package ch.tetrix.shared

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.scene2d.KTableWidget
import ktx.scene2d.label

enum class SnackbarPosition {
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT
}

object Snackbar {
    fun show(skin: Skin, stage: Stage, message: String, position: SnackbarPosition = SnackbarPosition.BOTTOM_CENTER) {
        val targetYOffset = 20f
        val background = skin.getDrawable("accent")

        val snackbar: Table = KTableWidget(skin).apply {
            pad(8f)
            background(background)

            label(message, "snackbar") {
                wrap = true
            }.cell(
                expandX = true,
                fillX = true,
                width = 300f,
                minHeight = 40f
            )

            pack()
        }

        val calculatedTargetX = when (position) {
            SnackbarPosition.BOTTOM_LEFT, SnackbarPosition.TOP_LEFT -> targetYOffset
            SnackbarPosition.BOTTOM_CENTER, SnackbarPosition.TOP_CENTER -> (stage.width - snackbar.width) / 2
            SnackbarPosition.BOTTOM_RIGHT, SnackbarPosition.TOP_RIGHT -> stage.width - snackbar.width - targetYOffset
        }

        val calculatedTargetY = when (position) {
            SnackbarPosition.BOTTOM_LEFT, SnackbarPosition.BOTTOM_CENTER, SnackbarPosition.BOTTOM_RIGHT -> targetYOffset
            SnackbarPosition.TOP_LEFT, SnackbarPosition.TOP_CENTER, SnackbarPosition.TOP_RIGHT -> stage.height - snackbar.height - targetYOffset
        }

        val calculatedInitialY = when (position) {
            SnackbarPosition.BOTTOM_LEFT, SnackbarPosition.BOTTOM_CENTER, SnackbarPosition.BOTTOM_RIGHT -> -snackbar.height
            SnackbarPosition.TOP_LEFT, SnackbarPosition.TOP_CENTER, SnackbarPosition.TOP_RIGHT -> stage.height
        }

        snackbar.setPosition(calculatedTargetX, calculatedInitialY)

        stage.addActor(snackbar)

        val sequence = Actions.sequence(
            Actions.moveTo(snackbar.x, calculatedTargetY, 0.5f), // Animate to final position
            Actions.delay(3.0f),
            Actions.parallel(
                Actions.moveTo(snackbar.x, calculatedInitialY, 0.5f), // Animate back off-screen
                Actions.fadeOut(0.5f)
            ),
            Actions.removeActor()
        )

        snackbar.addAction(sequence)
    }
}
