import com.badlogic.gdx.scenes.scene2d.Stage

// Base overlay interface
interface GameOverlay {
    fun show()
    fun hide()
    fun isVisible(): Boolean
    fun render(delta: Float)
    fun resize(width: Int, height: Int)
    fun dispose()
}

abstract class BaseOverlay : GameOverlay {
    val stage = Stage()
    private var visible = false

    override fun show() {
        visible = true
        stage.root.isVisible = true
    }

    override fun hide() {
        visible = false
        stage.root.isVisible = false
    }

    override fun isVisible(): Boolean = visible

    override fun render(delta: Float) {
        if (visible) {
            stage.act(delta)
            stage.draw()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}
