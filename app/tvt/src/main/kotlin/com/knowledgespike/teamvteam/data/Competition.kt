package com.knowledgespike.teamvteam.data

import kotlinx.serialization.Serializable


@Serializable
data class Competition(
    val title: String, val gender: String, val country: String, val outputDirectory: String,
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
    val authors: List<Author> = listOf(),
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

data class OpponentWithAuthors(val team: String, val author: List<Author>)