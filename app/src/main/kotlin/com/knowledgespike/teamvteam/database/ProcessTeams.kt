package com.knowledgespike.teamvteam.database

import com.knowledgespike.db.tables.references.MATCHES
import com.knowledgespike.db.tables.references.MATCHSUBTYPE
import com.knowledgespike.teamvteam.TeamNameToIds
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.teamvteam.data.TeamsAndOpponents
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.select
import java.sql.DriverManager


data class TeamPairDetails(val teamA: String, val teamB: String) {

    val teamAHighestScores = mutableListOf<TotalDao>()
    val teamALowestScores = mutableListOf<TotalDao>()
    val teamAHighestIndividualScore = mutableListOf<HighestScoreDao>()
    val teamABestBowlingInnings = mutableListOf<BestBowlingDao>()
    val teamABestBowlingMatch = mutableListOf<BestBowlingDao>()
    val teamABestFoW = mutableMapOf<Int, FowDetails>()

    val teamBHighestScores = mutableListOf<TotalDao>()
    val teamBLowestScores = mutableListOf<TotalDao>()
    val teamBHighestIndividualScore = mutableListOf<HighestScoreDao>()
    val teamBBestBowlingInnings = mutableListOf<BestBowlingDao>()
    val teamBBestBowlingMatch = mutableListOf<BestBowlingDao>()
    val teamBBestFoW = mutableMapOf<Int, FowDetails>()

}

class ProcessTeams(
    private val allTeams: TeamNameToIds,
) {

    private val idPairs: List<TeamsAndOpponents>

    private fun buildPairsOfTeamsThatMayCompete(): List<TeamsAndOpponents> {

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
        return pairs
    }

    init {
        idPairs = buildPairsOfTeamsThatMayCompete()
    }

    operator fun invoke(
        connectionString: String,
        userName: String,
        password: String,
        matchSubType: String,
        callback: (teamPairDetails: TeamPairDetails) -> Unit
    ) {

        val matchType: String = matchTypeFromSubType(matchSubType)


        val teamRecords = TeamRecords(userName, password, connectionString)
        for (teamsAndOpponents in idPairs) {
            if (didPairsCompete(connectionString, userName, password, teamsAndOpponents, matchSubType)) {
                // todo
                val teamPairDetails = TeamPairDetails(teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)
                getTeamRecords(teamPairDetails, teamRecords, teamsAndOpponents, matchType, matchSubType)
                getIndividualRecords(teamPairDetails, teamRecords, teamsAndOpponents, matchType, matchSubType)
                callback(teamPairDetails)
            }
        }
    }


    private fun didPairsCompete(
        connectionString: String,
        userName: String,
        password: String,
        teamsAndOpponents: TeamsAndOpponents,
        matchSubType: String
    ): Boolean {
        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)
            val result = context.select(count()).from(MATCHES).where(
                (MATCHES.HOMETEAMID.`in`(teamsAndOpponents.teamdIds)
                    .or(MATCHES.HOMETEAMID.`in`(teamsAndOpponents.opponentIds)))
                    .and(
                        MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.opponentIds)
                            .or(MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.teamdIds))
                    )
                    .and(
                        MATCHES.ID.`in`(
                            select(MATCHSUBTYPE.MATCHID).from(
                                MATCHSUBTYPE.where(
                                    MATCHSUBTYPE.MATCHTYPE.eq(
                                        matchSubType
                                    )
                                )
                            )
                        )
                    )
                    .and(MATCHES.VICTORYTYPE.notEqual(6))
            ).fetch().first().getValue(0, Int::class.java)

            return result != 0
        }
    }

    private fun getIndividualRecords(
        teamPairDetails: TeamPairDetails,
        teamRecords: TeamRecords,
        teamsAndOpponents: TeamsAndOpponents,
        matchType: String,
        matchSubType: String,
    ) {
        val teamParamA = TeamParams(
            teamsAndOpponents.teamdIds,
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamName,
            teamsAndOpponents.opponentsName,
            matchType,
            matchSubType
        )
        val teamParamB = TeamParams(
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamdIds,
            teamsAndOpponents.opponentsName,
            teamsAndOpponents.teamName,
            matchType,
            matchSubType
        )

        teamPairDetails.teamAHighestIndividualScore.addAll(
            teamRecords.getHighestIndividualScores(teamParamA)
        )

        teamPairDetails.teamBHighestIndividualScore.addAll(
            teamRecords.getHighestIndividualScores(teamParamB)
        )

        teamPairDetails.teamABestBowlingInnings.addAll(
            teamRecords.getBestBowlingInnings(teamParamA)
        )

        teamPairDetails.teamBBestBowlingInnings.addAll(
            teamRecords.getBestBowlingInnings(teamParamB)
        )

        teamPairDetails.teamABestBowlingMatch.addAll(
            teamRecords.getBestBowlingMatch(teamParamA)
        )

        teamPairDetails.teamBBestBowlingMatch.addAll(
            teamRecords.getBestBowlingMatch(teamParamB)
        )

        teamPairDetails.teamABestFoW.putAll(
            teamRecords.getHighestFoW(teamParamA)
        )

        teamPairDetails.teamBBestFoW.putAll(
            teamRecords.getHighestFoW(teamParamB)
        )
    }

    private fun getTeamRecords(
        teamPairDetails: TeamPairDetails,
        tt: TeamRecords,
        teamsAndOpponents: TeamsAndOpponents,
        matchType: String,
        matchSubType: String,
        limit: Int = 5
    ) {
        val teamParamA = TeamParams(
            teamsAndOpponents.teamdIds,
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamName,
            teamsAndOpponents.opponentsName,
            matchType,
            matchSubType
        )
        val teamParamB = TeamParams(
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamdIds,
            teamsAndOpponents.opponentsName,
            teamsAndOpponents.teamName,
            matchType,
            matchSubType
        )

        teamPairDetails.teamAHighestScores.addAll(
            tt.getHighestTotals(
                teamParamA
            )
        )

        teamPairDetails.teamBHighestScores.addAll(
            tt.getHighestTotals(
                teamParamB
            )
        )

        teamPairDetails.teamALowestScores.addAll(
            tt.getLowestTotals(
                teamParamA
            )
        )

        teamPairDetails.teamBLowestScores.addAll(
            tt.getLowestTotals(
                teamParamB
            )
        )
    }
}

private fun matchTypeFromSubType(matchType: String): String {
    return when (matchType) {
        "t" -> "t"
        "f" -> "f"
        "o" -> "o"
        "a" -> "a"
        "itt" -> "itt"
        "tt" -> "tt"
        "bbl" -> "tt"
        "ipl" -> "tt"
        "hund" -> "tt"

        "wt" -> "wt"
        "wf" -> "wf"
        "wo" -> "wo"
        "wa" -> "wa"
        "witt" -> "witt"
        "wtt" -> "wtt"
        "wbbl" -> "wtt"
        "wipl" -> "wtt"
        "whund" -> "wtt"
        "cpl" -> "tt"
        "wcpl" -> "wtt"
        "minc" -> "minc"
        "wc" -> "o"
        "wwc" -> "wo"
        else -> throw Exception("Unknown match sub type")
    }
}