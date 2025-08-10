package ch.tetrix.loading.components

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Value
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.progressBar

object LoadingViewBuilder {
    data class LoadingLayoutResult(
        val tableWidget: KTableWidget,
        val progressBar: ProgressBar,
        val promptLabel: Label,
        val assetLabel: Label,
        val statusLabel: Label
    )

    fun layout(
        skin: Skin,
    ): LoadingLayoutResult {
        lateinit var progressBarRef: ProgressBar
        lateinit var promptLabelRef: Label
        lateinit var assetLabelRef: Label
        lateinit var statusLabelRef: Label

        val table = KTableWidget(skin).apply {
            setFillParent(true)
            center()
            defaults().pad(4f).prefWidth(Value.percentWidth(.75f, this))
                .maxWidth(600f).center()

            row().space(16f)
            label("Tetrix", "title-large")

            row()
            statusLabelRef = label("Loading assets...")

            row()
            assetLabelRef = label("")

            row().fillX()
            progressBarRef = progressBar(0f, 1f, 0.01f, false, "default-horizontal")

            row()
            promptLabelRef = label("press any key to continue...").apply {
                isVisible = false
            }

        }
        return LoadingLayoutResult(table, progressBarRef, promptLabelRef, assetLabelRef, statusLabelRef)
    }
}
