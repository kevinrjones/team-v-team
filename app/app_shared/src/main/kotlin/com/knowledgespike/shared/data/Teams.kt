package com.knowledgespike.shared.data

import com.knowledgespike.db.tables.references.MATCHES
import com.knowledgespike.db.tables.references.MATCHSUBTYPE
import com.knowledgespike.shared.TeamPairHomePagesData
import kotlinx.datetime.LocalDateTime
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

fun buildPairsOfTeamsThatMayCompete(allTeams: TeamNameToIds, opponentsForTeam: Map<String, TeamNameToIds>): List<TeamsAndOpponents> {

    val pairs = ArrayList<TeamsAndOpponents>()
    val teamNames = allTeams.keys.toTypedArray()

    val totalNumberOfTeams = teamNames.size
    for (i in 0 until totalNumberOfTeams) {
        val teamIds = allTeams.get(teamNames[i])!!
        for (j in i + 1 until totalNumberOfTeams) {
            val opponentIds = allTeams.get(teamNames[j])!!
            pairs.add(TeamsAndOpponents(teamNames[i], teamIds, teamNames[j], opponentIds))
        }
    }

    for (teamName in opponentsForTeam.keys) {
        val opponents = opponentsForTeam[teamName] ?: mapOf()
        val teamId = allTeams.get(teamName) ?: listOf()

        opponents.keys.sorted().forEach { name ->
            val opponentIds = opponents[name] ?: listOf()
            pairs.add(TeamsAndOpponents(teamName, teamId, name, opponentIds))
        }
    }
    return pairs
}

fun getCountOfMatchesBetweenTeams(
    connectionString: String,
    userName: String,
    password: String,
    teamsAndOpponents: TeamsAndOpponents,
    matchSubType: String,
    dialect: SQLDialect
): MatchDto {

    val matchTypesToExclude = mutableListOf("t", "wt", "itt", "witt", "o", "wo")


    if (matchSubType == "minc")
        matchTypesToExclude.add("sec")

    DriverManager.getConnection(connectionString, userName, password).use { conn ->


        val context = DSL.using(conn, dialect)
        val result = context.select(
            DSL.count(),
            DSL.min(MATCHES.MATCHSTARTDATEASOFFSET).`as`("startDate"),
            DSL.max(MATCHES.MATCHSTARTDATEASOFFSET).`as`("endDate"),
        ).from(MATCHES).where(
            (MATCHES.HOMETEAMID.`in`(teamsAndOpponents.teamIds)
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
        ).fetch().first()

        val startDate: LocalDateTime = (result.getValue("startDate", Long::class.java) * 1000).toLocalDateTime()
        val endDate = (result.getValue("endDate", Long::class.java) * 1000).toLocalDateTime()
        return MatchDto(
            result.getValue(0, Int::class.java),
            startDate,
            endDate,
        )
    }
}

fun addPairToPage(
    competitionTeams: List<String>,
    team1: String,
    team2: String,
    pairsForPage: Map<String, TeamPairHomePagesData>
): Map<String, TeamPairHomePagesData> {

    val mutablePairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()
    mutablePairsForPage.putAll(pairsForPage)
    var teamName = team1
    var hasOwnPage = competitionTeams.contains(teamName)

    var pairEx = pairsForPage[teamName]

    if (pairEx == null) {
        val teamPairList = mutableListOf<Pair<String, String>>()
        val teamPair = Pair(team1, team2)
        teamPairList.add(teamPair)
        mutablePairsForPage[teamName] = TeamPairHomePagesData(hasOwnPage, teamPairList)
    } else {
        pairEx.teamPairDetails.add(Pair(team1, team2))
    }

    teamName = team2
    hasOwnPage = competitionTeams.contains(teamName)

    pairEx = pairsForPage[teamName]

    if (pairEx == null) {
        val teamPairList = mutableListOf<Pair<String, String>>()
        val teamPair = Pair(team1, team2)
        teamPairList.add(teamPair)
        mutablePairsForPage[teamName] = TeamPairHomePagesData(hasOwnPage, teamPairList)
    } else {
        pairEx.teamPairDetails.add(Pair(team1, team2))
    }

    return mutablePairsForPage
}

data class TeamParams(
    val teamIds: List<Int>,
    val opponentIds: List<Int>,
    val team: String,
    val opponents: String,
    val matchType: String,
    val matchSubType: String,
)

fun getTeamParams(
    teamsAndOpponents: TeamsAndOpponents,
    matchType: String,
    matchSubType: String
): Pair<TeamParams, TeamParams> {
    val teamParamA = TeamParams(
        teamsAndOpponents.teamIds,
        teamsAndOpponents.opponentIds,
        teamsAndOpponents.teamName,
        teamsAndOpponents.opponentsName,
        matchType,
        matchSubType
    )
    val teamParamB = TeamParams(
        teamsAndOpponents.opponentIds,
        teamsAndOpponents.teamIds,
        teamsAndOpponents.opponentsName,
        teamsAndOpponents.teamName,
        matchType,
        matchSubType
    )

    return Pair(teamParamA, teamParamB)
}
