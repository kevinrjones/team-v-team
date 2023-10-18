package com.knowledgespike.teamvteam.daos

import java.time.LocalDateTime


data class MatchDto(val count:  Int, val startDate: LocalDateTime, val endDate: LocalDateTime)
data class LowestScoreDto(
    val lowestAllOutScore: List<TotalDto>,
    val lowestCompleteScores: List<TotalDto>,
    val lowestIncompleteScores: List<TotalDto>
)

data class TotalDto(
    val team: String,
    val opponents: String,
    val total: Int,
    val wickets: Int,
    val declared: Boolean,
    val location: String,
    val seriesDate: String
)

data class HighestScoreDto(
    val name: String,
    val team: String,
    val opponents: String,
    val score: Int,
    val notOut: Boolean,
    val location: String,
    val seriesDate: String,
)

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

data class MostDismissalsDto(
    val name: String,
    val team: String,
    val opponents: String,
    val matches: Int,
    val dismissals: Int,
)

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

data class BestBowlingDto(
    val name: String,
    val team: String,
    val opponents: String,
    val wickets: Int,
    val runs: Int,
    val location: String,
    val seriesDate: String,
)

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

data class FoWDto(
    val team: String,
    val opponents: String,
    val location: String,
    val seriesDate: String,
    val partnership: Int,
    val wicket: Int,
    val undefeated: Boolean,
    val player1Name: String,
    val player1Score: Int,
    val player1NotOut: Boolean,
    val player2Name: String,
    val player2Score: Int,
    val player2NotOut: Boolean,
)

data class MultiPlayerFowDto(val total: Int, val wicket: Int, val playerDetails: List<FoWDto>)

data class FowDetails(val standardFow: List<FoWDto>, val multiPlayerFow: List<MultiPlayerFowDto>)

data class PossibleMultiPlayerPartnerships(val matchId: Int, val inningsOrder: Int)