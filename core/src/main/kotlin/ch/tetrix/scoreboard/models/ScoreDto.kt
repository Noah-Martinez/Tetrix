package ch.tetrix.scoreboard.models

data class ScoreDto(
    val id: Long? = null,
    val username: String = "",
    val score: Int = 0,
    val rank: Int = 0,
) {
    fun toScoreEntity() = ScoreEntity(this.id, this.username, this.score)
}
