package com.knowledgespike.teamvteam.data

import kotlinx.serialization.Serializable


@Serializable
data class Competition(
    val title: String, val gender: String, val country: String, val outputDirectory: String,
    val teams: List<Team>, val subType: String, val extraMessages: List<String>
)

@Serializable
data class Team(val team: String, val duplicates: List<String>, val opponents: List<String> = listOf())

data class TeamsAndOpponents(
    val teamName: String,
    val teamdIds: List<Int>,
    val opponentsName: String,
    val opponentIds: List<Int>
)