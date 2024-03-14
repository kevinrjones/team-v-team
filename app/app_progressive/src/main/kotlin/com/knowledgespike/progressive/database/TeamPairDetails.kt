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
        matchSubType: String
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
            teamRecords.getProgressivePartnershipRecordsForAll(teamParamA)
        )
        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecordsVsAll(teamParamB)
        )

    }

    fun getTeamRecords(connection: DatabaseConnection, teamAndIds: TeamAndIds, opponents: List<Int>, matchType: String, matchSubType: String) {
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
            teamRecords.getHighestTotalsForAll(
                teamParamA
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotalsVsAll(
                teamParamB
            )
        )

        lowestAllOutScores[0].addAll(
            teamRecords.getLowestAllOutTotalsForAll(
                teamParamA
            )
        )

        lowestAllOutScores[1].addAll(
            teamRecords.getLowestAllOutTotalsVsAll(
                teamParamB
            )
        )

    }

    fun getIndividualRecords(connection: DatabaseConnection, teamAndIds: TeamAndIds, opponents: List<Int>, matchType: String, matchSubType: String) {

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
            teamRecords.getHighestIndividualScoresForAll(teamParamA)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScoresVsAll(teamParamB)
        )

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInningsForAll(teamParamA)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInningsVsAll(teamParamB)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatchForAll(teamParamA)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatchVsAll(teamParamB)
        )

    }

}

class TeamPairDetails(val teams: Array<String>, val matchDto: MatchDto) {
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
        teamParams2: TeamParams
    ) {
        val teamRecords = TeamRecords(databaseConnection)

        bestFoW[0].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams1)
        )

        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams2)
        )
    }

    fun getTeamRecords(databaseConnection: DatabaseConnection, teamParamA: TeamParams, teamParamB: TeamParams) {
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