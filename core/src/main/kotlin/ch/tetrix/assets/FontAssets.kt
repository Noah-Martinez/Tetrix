package ch.tetrix.assets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter

enum class FontAssets(
    val path: String,
    val params: FreeTypeFontParameter.() -> Unit
) {
    Default(
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 12
        }
    ),
    Title(
        "fonts/PressStart2P-Regular.ttf",
        {
            size = 24
            shadowColor = Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.08f)
            shadowOffsetX = 5
            shadowOffsetY = 5
        }
    )
}
