package ch.tetrix.shared

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.scene2d.Scene2DSkin

/**
 * An abstract screen class (TxScreen) that extends functionality from the [KtxScreen] base class.
 * It provides a stage to manage UI components and uses a shared skin for visual styling.
 *
 * Responsibilities include clearing the screen with a background color defined in the skin,
 * updating and rendering the stage, and releasing resources upon disposal.
 *
 * Primary features:
 * - Manages a [Stage] for handling UI components and rendering.
 * - Automatically clears the screen during rendering with a skin-defined background color.
 * - Ensures resources such as the stage are properly disposed of when the screen is no longer needed.
 */
abstract class TxScreen: KtxScreen {
    protected val skin: Skin by lazy { Scene2DSkin.defaultSkin }
    protected abstract val stage: Stage

    override fun render(delta: Float) {
        super.render(delta)
        val backgroundColor = skin.getColor("primary")
        clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
    }
}
