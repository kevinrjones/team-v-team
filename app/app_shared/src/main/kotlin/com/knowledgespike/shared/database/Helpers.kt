package com.knowledgespike.shared.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.shared.data.*
import kotlinx.datetime.LocalDateTime
import org.jooq.*
import org.jooq.impl.DSL
import java.sql.DriverManager

fun checkIfShouldProcess(
    databaseConnection: DatabaseConnection,
    teamIds: List<Int>,
    opponentIds: List<Int>,
    matchType: String,
    dateOffset: Long,
    dialect: SQLDialect
): Boolean {
    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { connection ->
        val context = DSL.using(connection, dialect)
        val res = context.select(MATCHES.ID).from(MATCHES).where(
            (MATCHES.HOMETEAMID.`in`(teamIds)
                .or(MATCHES.HOMETEAMID.`in`(opponentIds)))
                .and(
                    MATCHES.AWAYTEAMID.`in`(opponentIds)
                        .or(MATCHES.AWAYTEAMID.`in`(teamIds))
                )
                .and(MATCHES.MATCHTYPE.eq(matchType))
                .and(MATCHES.ADDEDDATEASOFFSET.gt(dateOffset))
        ).fetch()

        return res.isNotEmpty
    }
}

fun getCountryIdsFromName(countries: List<String>, databaseConnection: DatabaseConnection): MutableList<Int> {
    val ids = mutableListOf<Int>()
    if (countries.isEmpty())
        return ids
    else {
        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = DSL.using(conn, databaseConnection.dialect)

            val result = context.select(COUNTRIES.COUNTRYID)
                .from(COUNTRIES)
                .where(COUNTRIES.COUNTRYNAME.`in`(countries))
                .fetch()

            for (r in result) {
                ids.add(r.get(0, Int::class.java))
            }

            return ids
        }
    }
}

fun getCountOfMatchesBetweenTeams(
    databaseConnection: DatabaseConnection,
    countryIds: List<Int>,
    teamsAndOpponents: TeamsAndOpponents,
    matchSubType: String,
    dialect: SQLDialect
): MatchDto {

    val matchTypesToExclude = mutableListOf("t", "wt", "itt", "witt", "o", "wo")


    if (matchSubType == "minc")
        matchTypesToExclude.add("sec")

    var whereClause = (MATCHES.HOMETEAMID.`in`(teamsAndOpponents.teamIds)
        .or(MATCHES.HOMETEAMID.`in`(teamsAndOpponents.opponentIds)))
        .and(
            MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.opponentIds)
                .or(MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.teamIds))
        )
        .and(
            MATCHES.ID.`in`(
                DSL.select(MATCHSUBTYPE.MATCHID).from(
                    MATCHSUBTYPE.where(
                        MATCHSUBTYPE.MATCHTYPE.eq(
                            matchSubType
                        )
                    )
                )
            )
        )
        .and(MATCHES.VICTORYTYPE.notEqual(6))
        .and(MATCHES.VICTORYTYPE.notEqual(11))
        .and(MATCHES.MATCHTYPE.notIn(matchTypesToExclude))

    if (countryIds.isNotEmpty())
        whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, dialect)
        val result = context.select(
            DSL.count(),
            DSL.min(MATCHES.MATCHSTARTDATEASOFFSET).`as`("startDate"),
            DSL.max(MATCHES.MATCHSTARTDATEASOFFSET).`as`("endDate"),
        ).from(MATCHES).where(whereClause)
            .fetch()
            .first()

        val startDate: LocalDateTime = (result.getValue("startDate", Long::class.java) * 1000).toLocalDateTime()
        val endDate = (result.getValue("endDate", Long::class.java) * 1000).toLocalDateTime()
        return MatchDto(
            result.getValue(0, Int::class.java),
            startDate,
            endDate,
        )
    }
}

fun getTeamIds(
    databaseConnection: DatabaseConnection,
    teams: List<TeamBase>,
    matchType: String,
    dialect: SQLDialect
): TeamNameToIds {


    val teamNameAndIds = mutableMapOf<String, List<Int>>()
    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
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




