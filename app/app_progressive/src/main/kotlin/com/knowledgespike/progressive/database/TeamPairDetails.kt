package com.knowledgespike.progressive.database

import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection

class TeamAndAllOpponentsDetails(val teamName: String, val matchDto: MatchDto) {
    val authors: MutableList<String> = mutableListOf("Kevin Jones")
    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val lowestAllOutScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val bestFoW = arrayListOf<MutableMap<Int, FowDetails>>(mutableMapOf(), mutableMapOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val bestBowlingInnings = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())
    val bestBowlingMatch = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())

    fun getFallOfWicketRecords(
        databaseConnection: DatabaseConnection,
        teamAndIds: TeamAndIds,
        opponents: List<Int>,
        matchType: String,
        matchSubType: String,
        overall: Boolean,
        startFrom: Long
    ) {
        val teamRecords = TeamRecords(databaseConnection)

        val teamParamA = TeamParams(
            teamAndIds.teamIds,
            opponents,
            teamAndIds.teamName,
            "All",
            matchType,
            matchSubType
        )

        val teamParamB = TeamParams(
            opponents,
            teamAndIds.teamIds,
            "All",
            teamAndIds.teamName,
            matchType,
            matchSubType
        )
        bestFoW[0].putAll(
            teamRecords.getProgressivePartnershipRecordsForSelectedTeamVsAllTeams(teamParamA, overall, startFrom)
        )
        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecordsForAllTeamsVsSelectedTeam(teamParamB, overall, startFrom)
        )

    }

    fun getTeamRecords(
        connection: DatabaseConnection,
        teamAndIds: TeamAndIds,
        opponents: List<Int>,
        matchType: String,
        matchSubType: String,
        overall: Boolean,
        startFrom: Long
    ) {
        val teamParamA = TeamParams(
            teamAndIds.teamIds,
            opponents,
            teamAndIds.teamName,
            "All",
            matchType,
            matchSubType
        )

        val teamParamB = TeamParams(
            opponents,
            teamAndIds.teamIds,
            "All",
            teamAndIds.teamName,
            matchType,
            matchSubType
        )

        val teamRecords = TeamRecords(connection)
        highestScores[0].addAll(
            teamRecords.getHighestTotalsForSelectedTeamVsAllTeams(
                teamParamA,
                overall,
                startFrom
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotalsForAllTeamsVsSelectedTeam(
                teamParamB,
                overall,
                startFrom
            )
        )

        lowestAllOutScores[0].addAll(
            teamRecords.getLowestAllOutTotalsForSelectedTeamVsAllTeams(
                teamParamA,
                overall,
                startFrom
            )
        )

        lowestAllOutScores[1].addAll(
            teamRecords.getLowestAllOutTotalsForAllTeamsVsSelectedTeam(
                teamParamB,
                overall,
                startFrom
            )
        )

    }

    fun getIndividualRecords(
        connection: DatabaseConnection,
        teamAndIds: TeamAndIds,
        opponents: List<Int>,
        matchType: String,
        matchSubType: String,
        overall: Boolean,
        startFrom: Long
    ) {

        val teamParamA = TeamParams(
            teamAndIds.teamIds,
            opponents,
            teamAndIds.teamName,
            "All",
            matchType,
            matchSubType
        )

        val teamParamB = TeamParams(
            opponents,
            teamAndIds.teamIds,
            "All",
            teamAndIds.teamName,
            matchType,
            matchSubType
        )

        val teamRecords = TeamRecords(connection)

        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScoresForSelectedTeamVsAllTeams(teamParamA,overall, startFrom)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScoresForAllTeamsVsSelectedTeam(teamParamB,overall, startFrom)
        )

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInningsForSelectedTeamVsAllTeams(teamParamA,overall, startFrom)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInningsForAllTeamsVsSelectedTeam(teamParamB,overall, startFrom)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatchForSelectedTeamVsAllTeams(teamParamA,overall, startFrom)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatchForAllTeamsVsSelectedTeam(teamParamB,overall, startFrom)
        )

    }

}

class TeamPairDetails(val teams: Array<String>, val matchDto: MatchDto, val startFrom: Long) {
    val authors: MutableList<String> = mutableListOf("Kevin Jones")
    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val lowestAllOutScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val bestFoW = arrayListOf<MutableMap<Int, FowDetails>>(mutableMapOf(), mutableMapOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val bestBowlingInnings = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())
    val bestBowlingMatch = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())

    fun getFallOfWicketRecords(
        databaseConnection: DatabaseConnection,
        teamParams1: TeamParams,
        teamParams2: TeamParams,
        overall: Boolean
    ) {
        val teamRecords = TeamRecords(databaseConnection)

        bestFoW[0].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams1, overall, startFrom )
        )

        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams2, overall, startFrom )
        )
    }

    fun getTeamRecords(
        databaseConnection: DatabaseConnection,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
    ) {
        val teamRecords = TeamRecords(databaseConnection)
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                teamParamA
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                teamParamB
            )
        )

        lowestAllOutScores[0].addAll(
            teamRecords.getLowestAllOutTotals(
                teamParamA
            )
        )

        lowestAllOutScores[1].addAll(
            teamRecords.getLowestAllOutTotals(
                teamParamB
            )
        )

    }

    fun getIndividualRecords(databaseConnection: DatabaseConnection, teamParamA: TeamParams, teamParamB: TeamParams) {
        val teamRecords = TeamRecords(databaseConnection)
        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(teamParamA)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(teamParamB)
        )

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInnings(teamParamA)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInnings(teamParamB)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatch(teamParamA)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatch(teamParamB)
        )

    }

}