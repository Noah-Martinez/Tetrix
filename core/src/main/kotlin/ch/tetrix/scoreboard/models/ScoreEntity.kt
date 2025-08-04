package ch.tetrix.scoreboard.models

data class ScoreEntity(
    val id: Long? = null,
    val username: String = "",
    val score: Int = 0,
)
