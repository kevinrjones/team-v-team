package com.knowledgespike.shared.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.types.TeamIdsAndValidDate
import org.jooq.*
import org.jooq.impl.DSL
import java.sql.DriverManager

fun checkIfShouldProcess(
    databaseConnection: DatabaseConnection,
    teamIds: List<Int>,
    opponentIds: List<Int>,
    matchType: String,
    dateOffset: Long,
    dialect: SQLDialect,
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
    overall: Boolean,
    startFrom: Long
): MatchDto {

    val matchTypesToExclude = mutableListOf("t", "wt", "itt", "witt", "o", "wo")


    if (matchSubType == "minc")
        matchTypesToExclude.add("sec")

    var whereClause = EXTRAMATCHDETAILS.TEAMID.`in`(teamsAndOpponents.teamIds)
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
        .and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

    // When calculating the records va 'all' teams we can do two things. Calculate against 'all' the teams
    // in this set of teams (all IPL teams say) or calculate their overall record against all other teams
    // The 'overall' flag lets us determine this
    if (!(overall && teamsAndOpponents.opponentsName.lowercase() == "all"))
        whereClause = whereClause.and(EXTRAMATCHDETAILS.OPPONENTSID.`in`(teamsAndOpponents.opponentIds))

    if (countryIds.isNotEmpty())
        whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))



    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, databaseConnection.dialect)
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
            if (r.size == 0) {
                return MatchDto(
                    0,
                    0L.toLocalDateTime(),
                    0L.toLocalDateTime(),
                )
            }
            if (teamsAndOpponents.teamIds.size == 1 && teamsAndOpponents.teamIds[0] == 710 && matchSubType == "wwc") {
                println("")
            }
            var wins = 0
            var losses = 0
            var draws = 0
            var ties = 0
            var abandoned = 0
            var cancelled = 0
            var abandonedAsDraw = 0
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
                    64 -> abandonedAsDraw = count
                }

                startDate = (result.getValue("startDate", Long::class.java) * 1000).toLocalDateTime()
                endDate = (result.getValue("endDate", Long::class.java) * 1000).toLocalDateTime()
            }
            val matches = wins + losses + draws + ties + abandonedAsDraw
            return MatchDto(
                matches,
                startDate,
                endDate,
                firstTeamWins = wins,
                firstTeamLosses = losses,
                draws,
                ties,
                abandoned,
                abandonedAsDraw,
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
    country: String?,
    matchType: String,
    ): Map<String, TeamIdsAndValidDate> {


    val teamNameAndIds = mutableMapOf<String, TeamIdsAndValidDate>()
    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, databaseConnection.dialect)

        for (t in teams) {
            val ids = mutableListOf<Int>()
            val team = t.team.trim()
            if (team.isNotEmpty()) {
                val teamIds = getTeamIdsFrom(context, team, matchType, country)

                ids.addAll(teamIds)
                t.duplicates.forEach { duplicate ->
                    val duplicateTeamIds = getTeamIdsFrom(context, duplicate, matchType, country)
                    ids.addAll(duplicateTeamIds)
                }
            }
            teamNameAndIds[team] = TeamIdsAndValidDate(ids, t.validFrom)
        }
    }

    val caTeamIds = teams.flatMap { it.excludeTeamIds }

    val teamsIds: List<Int> = convertCaTeamIdsToTeamIds(caTeamIds, databaseConnection)

    teamNameAndIds.forEach { (team, ids) ->
        val updatedIds = ids.teamIds.filter { !teamsIds.contains(it) }
        teamNameAndIds[team] = ids.copy(teamIds = updatedIds)
    }
    return teamNameAndIds
}

fun convertCaTeamIdsToTeamIds(caTeamIds: List<Int>, databaseConnection: DatabaseConnection): List<Int> {
    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, databaseConnection.dialect)

        return context.select(TEAMS.ID)
            .from(TEAMS)
            .where(TEAMS.TEAMID.`in`(caTeamIds))
            .fetch()
            .getValues(TEAMS.ID, Int::class.java)
    }
}

fun getTeamIdsFrom(context: DSLContext, team: String, matchType: String, country: String?): List<Int> {

    /*
    * If the incoming selection data (eg bdesh_domestic.json) has a country then we have to filter the teams names by country
    * This is to prevent teams with names that are common across countries being included in the query, e.g. there are
    * 'Central Zone' teams in Bangladesh, India and Pakistan
    *
    * To prevent that I build this query that has
    * MatchType = 'f' and (Teams.CountryId = countryId  or teams.CountryId is null);
     */
    val countryCondition = if (country != null) {
        val countryIdResult = context
            .select(COUNTRIES.ID)
            .from(COUNTRIES)
            .where(COUNTRIES.COUNTRYNAME.eq(country))
            .fetch()

        val countryId = countryIdResult.getValues(COUNTRIES.ID, Int::class.java).first()

        TEAMSMATCHTYPES.MATCHTYPE.eq(matchType).and(TEAMS.COUNTRYID.eq(countryId).or(TEAMS.COUNTRYID.isNull))

    } else {
        TEAMSMATCHTYPES.MATCHTYPE.eq(matchType)
    }

    val idRecord = context
        .select(TEAMS.ID)
        .from(TEAMS)
        .join(TEAMSMATCHTYPES).on(TEAMSMATCHTYPES.TEAMID.eq(TEAMS.ID))
        .where(TEAMS.NAME.eq(team))
        .and(countryCondition)
        .fetch()

    return idRecord.getValues(TEAMS.ID, Int::class.java)
}




