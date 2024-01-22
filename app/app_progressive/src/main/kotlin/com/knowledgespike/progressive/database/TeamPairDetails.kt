package com.knowledgespike.progressive.database

import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import org.jooq.SQLDialect

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

        val teamParams = TeamParams(
            teamAndIds.teamIds,
            opponents,
            teamAndIds.teamName,
            "all",
            matchType,
            matchSubType
        )
        bestFoW[0].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams)
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