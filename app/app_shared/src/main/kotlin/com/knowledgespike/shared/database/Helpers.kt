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

    var whereClause = (EXTRAMATCHDETAILS.TEAMID.`in`(teamsAndOpponents.teamIds)
        .and(EXTRAMATCHDETAILS.OPPONENTSID.`in`(teamsAndOpponents.opponentIds)))
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
        .and(MATCHES.MATCHTYPE.notIn(matchTypesToExclude))

    if (countryIds.isNotEmpty())
        whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, dialect)
        val r = context.selectDistinct(
            EXTRAMATCHDETAILS.TEAMID,
            EXTRAMATCHDETAILS.OPPONENTSID,
            EXTRAMATCHDETAILS.RESULT,
            DSL.count(EXTRAMATCHDETAILS.RESULT).over().partitionBy(EXTRAMATCHDETAILS.RESULT)
                .orderBy(EXTRAMATCHDETAILS.RESULT).`as`("count"),
            DSL.min(MATCHES.MATCHSTARTDATEASOFFSET).over().`as`("startDate"),
            DSL.max(MATCHES.MATCHSTARTDATEASOFFSET).over().`as`("endDate"),
        ).from(EXTRAMATCHDETAILS)
            .join(MATCHES).on(MATCHES.ID.eq(EXTRAMATCHDETAILS.MATCHID))
            .where(whereClause)
            .fetch()

        try {
            if(r.size == 0) {
                return MatchDto(
                    0,
                    0L.toLocalDateTime(),
                    0L.toLocalDateTime(),
                )
            }
            if(teamsAndOpponents.teamIds.size == 1 && teamsAndOpponents.teamIds[0] == 710 && matchSubType == "wwc") {
                println("")
            }
            var wins = 0
            var losses = 0
            var draws = 0
            var ties = 0
            var abandoned = 0
            var cancelled = 0
            var startDate = 0L.toLocalDateTime()
            var endDate = 0L.toLocalDateTime()

            r.forEach { result ->


                val resultType = result.getValue("Result", Int::class.java)
                val count = result.getValue("count", Int::class.java)


                when (resultType) {
                    1 -> wins = count
                    2 -> losses = count
                    4 -> draws = count
                    8 -> ties = count
                    16 -> abandoned = count
                    32 -> cancelled = count
                }

                startDate = (result.getValue("startDate", Long::class.java) * 1000).toLocalDateTime()
                endDate = (result.getValue("endDate", Long::class.java) * 1000).toLocalDateTime()
            }
            val matches = wins + losses + draws + ties
            return MatchDto(
                matches,
                startDate,
                endDate,
                firstTeamWins = wins,
                firstTeamLosses = losses,
                draws,
                ties,
                abandoned,
                cancelled
            )
        } catch (e: Exception) {
            throw e
        }
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




