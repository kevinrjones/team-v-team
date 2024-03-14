package com.knowledgespike.teamvteam.database

import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.teamvteam.daos.*

data class TeamPairDetails(val teams: Array<String>, val matchDto: MatchDto) {

    val strikeRateScoreLimit: Int = 20
    val strikeRateLowerBallsLimit = 10
    val strikeRateUpperBallsLimit = 20
    val economyRateOversLimit = 3
    val authors: MutableList<String> = mutableListOf("Kevin Jones")

    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val highestIndividualStrikeRates = arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
    val highestIndividualStrikeRatesWithLimit =
        arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
    val lowestIndividualStrikeRates = arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
    val lowestIndividualStrikeRatesWithLimit = arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
    val mostFours = arrayListOf<MutableList<BoundariesDto>>(mutableListOf(), mutableListOf())
    val mostSixes = arrayListOf<MutableList<BoundariesDto>>(mutableListOf(), mutableListOf())
    val mostBoundaries = arrayListOf<MutableList<BoundariesDto>>(mutableListOf(), mutableListOf())

    val bestBowlingInnings = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())
    val bestBowlingMatch = arrayListOf<MutableList<BestBowlingDto>>(mutableListOf(), mutableListOf())

    val bestBowlingSRInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())
    val bestBowlingSRWithLimitInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())
    val bestBowlingERInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())
    val bestBowlingERWithLimitInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())
    val worstBowlingERInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())
    val worstBowlingERWithLimitInnings = arrayListOf<MutableList<BowlingRatesDto>>(mutableListOf(), mutableListOf())

    val bestFoW = arrayListOf<MutableMap<Int, FowDetails>>(mutableMapOf(), mutableMapOf())

    val mostRunsVsOpposition = arrayListOf<MutableList<MostRunsDto>>(mutableListOf(), mutableListOf())
    val mostWicketsVsOpposition = arrayListOf<MutableList<MostWicketsDto>>(mutableListOf(), mutableListOf())
    val mostCatchesVsOpposition = arrayListOf<MutableList<MostDismissalsDto>>(mutableListOf(), mutableListOf())
    val mostStumpingsVsOpposition = arrayListOf<MutableList<MostDismissalsDto>>(mutableListOf(), mutableListOf())

    var teamAllLowestScores = arrayListOf<LowestScoreDto>()


    val nonFcMatchTypes = listOf("o", "a", "wo", "wa", "tt", "itt", "wtt", "witt")

    fun addTeamData(
        databaseConnection: DatabaseConnection,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams
    ) {
        val teamRecords = TeamRecords(databaseConnection)
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamA
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamB
            )
        )

        var lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamA)
        var lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamA)
        var lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamA)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))

        lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamB)
        lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamB)
        lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamB)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))
    }

    fun addIndividualData(
        databaseConnection: DatabaseConnection,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        matchType: String
    ) {

        val teamRecords = TeamRecords(databaseConnection)
        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamA)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamB)
        )


        if (nonFcMatchTypes.contains(matchType)) {
            highestIndividualStrikeRates[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA)
            )
            highestIndividualStrikeRates[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB)
            )

            highestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA, strikeRateScoreLimit)
            )
            highestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB, strikeRateScoreLimit)
            )

            lowestIndividualStrikeRates[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateLowerBallsLimit)
            )
            lowestIndividualStrikeRates[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateLowerBallsLimit)
            )

            lowestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateUpperBallsLimit)
            )
            lowestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateUpperBallsLimit)
            )


            mostSixes[0].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamA)
            )
            mostSixes[1].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamB)
            )

            mostFours[0].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamA)
            )
            mostFours[1].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamB)
            )

            mostBoundaries[0].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamA)
            )
            mostBoundaries[1].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamB)
            )
        }

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamA)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamB)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamA)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamB)
        )


        bestFoW[0].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamA)
        )

        bestFoW[1].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamB)
        )

        if (nonFcMatchTypes.contains(matchType)) {
            bestBowlingSRInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA)
            )
            bestBowlingSRInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB)
            )

            bestBowlingSRWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA, economyRateOversLimit)
            )
            bestBowlingSRWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB, economyRateOversLimit)
            )

            bestBowlingERInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA)
            )
            bestBowlingERInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB)
            )

            bestBowlingERWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA, economyRateOversLimit)
            )
            bestBowlingERWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB, economyRateOversLimit)
            )

            worstBowlingERInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA)
            )
            worstBowlingERInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB)
            )

            worstBowlingERWithLimitInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA, economyRateOversLimit)
            )
            worstBowlingERWithLimitInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB, economyRateOversLimit)
            )
        }

        mostRunsVsOpposition[0].addAll(
            teamRecords.getMostRuns(countryIds, teamParamA)
        )


        mostRunsVsOpposition[1].addAll(
            teamRecords.getMostRuns(countryIds, teamParamB)
        )

        mostWicketsVsOpposition[0].addAll(
            teamRecords.getMostWickets(countryIds, teamParamA)
        )
        mostWicketsVsOpposition[1].addAll(
            teamRecords.getMostWickets(countryIds, teamParamB)
        )

        mostCatchesVsOpposition[0].addAll(
            teamRecords.getMostCatches(countryIds, teamParamA)
        )
        mostCatchesVsOpposition[1].addAll(
            teamRecords.getMostCatches(countryIds, teamParamB)
        )

        mostStumpingsVsOpposition[0].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamA)
        )
        mostStumpingsVsOpposition[1].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamB)
        )

    }

}