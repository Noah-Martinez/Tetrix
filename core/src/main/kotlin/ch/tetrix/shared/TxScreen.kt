package ch.tetrix.shared

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.scene2d.Scene2DSkin

/**
 * Terix implementation of KtxScreen
 *
 * provides basic implementations of:
 * - render()
 * - resize()
 * - dispose()
 *
 * and the default skin
 */
abstract class TxScreen: KtxScreen {
    protected val skin: Skin by lazy { Scene2DSkin.defaultSkin }
    protected abstract val stage: Stage

    /** resizes the viewport */
    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        stage.viewport.update(width, height, true)
    }

    /** clears screen & updates and renders the stage */
    override fun render(delta: Float) {
        super.render(delta)
        val backgroundColor = skin.getColor("primary")
        clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)
        stage.viewport.apply()
    }

    /** disposes the screen and the stage */
    override fun dispose() {
        super.dispose()
        stage.dispose()
    }
}
