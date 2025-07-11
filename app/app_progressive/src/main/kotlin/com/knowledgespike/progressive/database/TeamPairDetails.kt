package com.knowledgespike.progressive.database

import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.types.TeamIdAndValidDate
import org.jooq.SQLDialect
import java.sql.Connection

class TeamAndAllOpponentsDetails(val teamName: String, val matchDto: MatchDto, private val nameUpdates: List<NameUpdate>) {
    val authors: MutableList<String> = mutableListOf("Kevin Jones")
    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val lowestAllOutScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val bestFoW = arrayListOf<MutableMap<Int, FowDetails>>(mutableMapOf(), mutableMapOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val bestBowlingInnings = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())
    val bestBowlingMatch = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())

    fun getFallOfWicketRecords(
        databaseConnectionDetails: Connection,
        dialect: SQLDialect,
        teamAndIds: TeamAndIds,
        opponents: List<TeamIdAndValidDate>,
        matchType: String,
        matchSubType: String,
        overall: Boolean
    ) {
        val teamRecords = TeamRecords(databaseConnectionDetails, dialect, nameUpdates)

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
            teamRecords.getProgressivePartnershipRecordsForSelectedTeamVsAllTeams(
                teamParamA,
                overall,
                matchDto.matchIds
            )
        )
        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecordsForAllTeamsVsSelectedTeam(
                teamParamB,
                overall,
                matchDto.matchIds
            )
        )

    }

    fun getTeamRecords(
        connection: Connection,
        dialect: SQLDialect,
        teamAndIds: TeamAndIds,
        opponents: List<TeamIdAndValidDate>,
        matchType: String,
        matchSubType: String,
        overall: Boolean,
//        startFrom: Long
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

        val teamRecords = TeamRecords(connection, dialect, nameUpdates)
        highestScores[0].addAll(
            teamRecords.getHighestTotalsForSelectedTeamVsAllTeams(
                teamParamA,
                overall,
                matchDto.matchIds
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotalsForAllTeamsVsSelectedTeam(
                teamParamB,
                overall,
                matchDto.matchIds
            )
        )

        lowestAllOutScores[0].addAll(
            teamRecords.getLowestAllOutTotalsForSelectedTeamVsAllTeams(
                teamParamA,
                overall,
                matchDto.matchIds
            )
        )

        lowestAllOutScores[1].addAll(
            teamRecords.getLowestAllOutTotalsForAllTeamsVsSelectedTeam(
                teamParamB,
                overall,
                matchDto.matchIds
            )
        )

    }

    fun getIndividualRecords(
        connection: Connection,
        dialect: SQLDialect,
        teamAndIds: TeamAndIds,
        opponents: List<TeamIdAndValidDate>,
        matchType: String,
        matchSubType: String,
        overall: Boolean,
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

        val teamRecords = TeamRecords(connection, dialect, nameUpdates)

        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScoresForSelectedTeamVsAllTeams(teamParamA, overall, 
                matchDto.matchIds)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScoresForAllTeamsVsSelectedTeam(teamParamB, overall, 
                matchDto.matchIds)
        )

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInningsForSelectedTeamVsAllTeams(teamParamA, overall, 
                matchDto.matchIds)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInningsForAllTeamsVsSelectedTeam(teamParamB, overall, 
                matchDto.matchIds)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatchForSelectedTeamVsAllTeams(teamParamA, overall, 
                matchDto.matchIds)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatchForAllTeamsVsSelectedTeam(teamParamB, overall, 
                matchDto.matchIds)
        )

    }

}

class TeamPairDetails(val teams: Array<String>, val matchDto: MatchDto, private val nameUpdates: List<NameUpdate>) {
    val authors: MutableList<String> = mutableListOf("Kevin Jones")
    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val lowestAllOutScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val bestFoW = arrayListOf<MutableMap<Int, FowDetails>>(mutableMapOf(), mutableMapOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val bestBowlingInnings = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())
    val bestBowlingMatch = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())

    fun getFallOfWicketRecords(
        databaseConnection: Connection,
        dialect: SQLDialect,
        teamParams1: TeamParams,
        teamParams2: TeamParams,
        overall: Boolean,
        matchIds: List<Int>
    ) {
        val teamRecords = TeamRecords(databaseConnection, dialect, nameUpdates)

        bestFoW[0].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams1, overall,matchIds)
        )

        bestFoW[1].putAll(
            teamRecords.getProgressivePartnershipRecords(teamParams2, overall,  matchIds)
        )
    }

    fun getTeamRecords(
        databaseConnection: Connection,
        dialect: SQLDialect,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        matchIds: List<Int>
    ) {
        val teamRecords = TeamRecords(databaseConnection, dialect, nameUpdates)
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                teamParamA, matchIds
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                teamParamB, matchIds
            )
        )

        lowestAllOutScores[0].addAll(
            teamRecords.getLowestAllOutTotals(
                teamParamA, matchIds
            )
        )

        lowestAllOutScores[1].addAll(
            teamRecords.getLowestAllOutTotals(
                teamParamB, matchIds
            )
        )

    }

    fun getIndividualRecords(
        databaseConnection: Connection,
        dialect: SQLDialect,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        matchIds: List<Int>
    ) {
        val teamRecords = TeamRecords(databaseConnection, dialect, nameUpdates)
        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(teamParamA, matchIds)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(teamParamB, matchIds)
        )

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInnings(teamParamA, matchIds)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInnings(teamParamB, matchIds)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatch(teamParamA, matchIds)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatch(teamParamB, matchIds)
        )

    }

}