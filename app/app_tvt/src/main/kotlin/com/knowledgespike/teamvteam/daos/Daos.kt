package com.knowledgespike.teamvteam.daos

import com.knowledgespike.shared.data.TotalDto
import kotlinx.serialization.Serializable

@Serializable
data class LowestScoreDto(
    val lowestAllOutScore: List<TotalDto>,
    val lowestCompleteScores: List<TotalDto>,
    val lowestIncompleteScores: List<TotalDto>
)

@Serializable
data class MostRunsDto(
    val name: String,
    val team: String,
    val opponents: String,
    val matches: Int,
    val runs: Int,
    val innings: Int,
    val notOuts: Int,
    val average: Double,
    val hs: String
)

@Serializable
data class MostDismissalsDto(
    val name: String,
    val team: String,
    val opponents: String,
    val matches: Int,
    val dismissals: Int,
)

@Serializable
data class MostWicketsDto(
    val name: String,
    val team: String,
    val opponents: String,
    val matches: Int,
    val balls: Int,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val bbruns: Int,
    val bbwickets: Int,
    val average: Double
)

@Serializable
data class StrikeRateDto(
    val name: String,
    val team: String,
    val opponents: String,
    val strikeRate: Double,
    val runs: Int,
    val balls: Int,
    val location: String,
    val seriesDate: String,
)

@Serializable
data class BoundariesDto(
    val name: String,
    val team: String,
    val opponents: String,
    val boundaries: Int,
    val fours: Int,
    val sixes: Int,
    val location: String,
    val seriesDate: String,
)

@Serializable
data class BestBowlingDto(
    val name: String,
    val team: String,
    val opponents: String,
    val wickets: Int,
    val runs: Int,
    val location: String,
    val seriesDate: String,
)

@Serializable
data class BowlingRatesDto(
    val name: String,
    val team: String,
    val opponents: String,
    val overs: String,
    val balls: Int,
    val maidens: Int,
    val wickets: Int,
    val runs: Int,
    val sr: Double,
    val location: String,
    val seriesDate: String,
)


@Serializable
data class PossibleMultiPlayerPartnerships(val matchId: Int, val inningsOrder: Int)

