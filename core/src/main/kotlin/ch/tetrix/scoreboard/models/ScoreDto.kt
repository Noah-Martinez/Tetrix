package ch.tetrix.scoreboard.models

data class ScoreDto(
    val id: Long?,
    val username: String = "",
    val score: Int = 0,
    val rank: Int,
) {
    fun toScoreEntity() = ScoreEntity(this.id, this.username, this.score)
}
