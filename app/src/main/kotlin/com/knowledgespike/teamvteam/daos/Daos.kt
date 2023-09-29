package com.knowledgespike.teamvteam.daos

data class TotalDao(
    val team: String,
    val opponents: String,
    val total: Int,
    val wickets: Int,
    val declared: Boolean,
    val location: String,
    val seriesDate: String
)

data class HighestScoreDao(
    val name: String,
    val team: String,
    val opponents: String,
    val score: Int,
    val notOut: Boolean,
    val location: String,
    val seriesDate: String,
)

data class BestBowlingDao(
    val name: String,
    val team: String,
    val opponents: String,
    val wickets: Int,
    val runs: Int,
    val location: String,
    val seriesDate: String,
)

data class FoWDao(
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

data class MultiPlayerFowDao(val total: Int, val wicket: Int, val playerDetails: List<FoWDao>)

data class FowDetails(val standardFow: List<FoWDao>, val multiPlayerFow: List<MultiPlayerFowDao>)

data class PossibleMultiPlayerPartnerships(val matchId: Int, val inningsOrder: Int)