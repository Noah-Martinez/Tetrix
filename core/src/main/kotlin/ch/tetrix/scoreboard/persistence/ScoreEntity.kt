package ch.tetrix.scoreboard.persistence

data class ScoreEntity(
    val id: Long? = null,
    val username: String = "",
    val score: Int = 0,
)
