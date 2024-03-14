package com.knowledgespike.shared.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
    val title: String, val gender: String, val countries: List<String> = emptyList(), val outputDirectory: String,
    val teams: List<Team>, val subType: String, val extraMessages: List<String>
)

interface TeamBase {
    val team: String
    val duplicates: List<String>
}

@Serializable
data class Opponent(override val team: String, override val duplicates: List<String>) : TeamBase

@Serializable
data class Team(
    override val team: String,
    val authors: List<Author> = emptyList(),
    override val duplicates: List<String>,
    val opponents: List<Opponent> = listOf()
) : TeamBase


@Serializable
data class Author(val opponent: String, val name: String)

data class TeamsAndOpponents(
    val teamName: String,
    val teamIds: List<Int>,
    val opponentsName: String,
    val opponentIds: List<Int>
)

data class TeamAndIds(
    val teamName: String,
    val teamIds: List<Int>
)

data class TeamWithAuthors(val team: String, val author: List<Author>)

@Serializable
data class MatchDto(val count: Int, val startDate: LocalDateTime, val endDate: LocalDateTime)

@Serializable
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
    val player1Position: Int,
    val player2Name: String,
    val player2Score: Int,
    val player2NotOut: Boolean,
    val player2Position: Int,
    val possibleInvalid: Boolean = false
)

@Serializable
data class MultiPlayerFowDto(val total: Int, val unbroken: Boolean, val wicket: Int, val playerDetails: List<FoWDto>)


@Serializable
data class FowDetails(val standardFow: List<FoWDto>, val multiPlayerFow: List<MultiPlayerFowDto>)

@Serializable
data class TeamPairHomePagesJson(
    val mainTeamName: String,
    val teamNames: List<Pair<String, String>>,
    val matchDesignator: String,
    val matchType: String
)

@Serializable
data class CompetitionIndexPage(
    val teamNames: List<String>,
    val matchSubType: String,
    val gender: String,
    val title: String,
    val extraMessages: List<String>
)

@Serializable
data class TotalDto(
    val team: String,
    val opponents: String,
    val total: Int,
    val wickets: Int,
    val declared: Boolean,
    val location: String,
    val seriesDate: String
)

@Serializable
data class HighestScoreDto(
    val name: String,
    val team: String,
    val opponents: String,
    val score: Int,
    val notOut: Boolean,
    val location: String,
    val seriesDate: String,
)


/**
 * If teams have 'opponents' in the JSON then those opponents don't have a top level HTML page as it is never linked
 * to from any other page. Only the A v B teams have top level pages (see ou_v_cu.json for an example)
 */
data class TeamPairHomePagesData(
    val shouldHaveOwnPage: Boolean,
    val teamPairDetails: MutableList<Pair<String, String>>
)