package ch.tetrix.scoreboard.persistence

data class ScoreEntity(
    val id: Long?,
    val username: String,
    val score: Int = 0,
)
