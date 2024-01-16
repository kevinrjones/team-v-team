package com.knowledgespike.shared.data

import com.knowledgespike.db.tables.references.TEAMS
import com.knowledgespike.db.tables.references.TEAMSMATCHTYPES
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import kotlin.io.path.isRegularFile


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



fun getAllCompetitions(dataDirectory: String): List<Competition> {
    val allCompetitions = mutableListOf<Competition>()
    Files.list(Paths.get(dataDirectory))
        .filter { it.isRegularFile() }
        .filter {
            val fileName = it.fileName.toString()
            val ret = fileName.endsWith("json")

            ret
        }.forEach {
            val file = it.toFile()
            val data: String = file.readText()

            allCompetitions.addAll(Json.decodeFromString<List<Competition>>(data))
        }
    return allCompetitions
}

typealias TeamNameToIds = Map<String, List<Int>>

fun getTeamIds(
    connectionString: String,
    userName: String,
    password: String,
    teams: List<TeamBase>,
    matchType: String,
    dialect: SQLDialect
): TeamNameToIds {


    val teamNameAndIds = mutableMapOf<String, List<Int>>()
    DriverManager.getConnection(connectionString, userName, password).use { conn ->
        val context = DSL.using(conn, dialect)

        for (t in teams) {
            val ids = mutableListOf<Int>()
            val team = t.team.trim()
            if (team.isNotEmpty()) {
                val teamIds = getTeamIdsFrom(context, team, matchType)

                ids.addAll(teamIds)
                t.duplicates.forEach { duplicate ->
                    val duplicateTeamIds = getTeamIdsFrom(context, duplicate, matchType)
                    ids.addAll(duplicateTeamIds)
                }
            }
            teamNameAndIds[team] = ids
        }

    }

    return teamNameAndIds
}

fun getTeamIdsFrom(context: DSLContext, team: String, matchType: String): List<Int> {
    val idRecord = context
        .select(TEAMS.ID)
        .from(TEAMS)
        .join(TEAMSMATCHTYPES).on(TEAMSMATCHTYPES.TEAMID.eq(TEAMS.ID))
        .where(TEAMS.NAME.eq(team))
        .and(TEAMSMATCHTYPES.MATCHTYPE.eq(matchType))
        .fetch()

    return idRecord.getValues(TEAMS.ID, Int::class.java)
}
