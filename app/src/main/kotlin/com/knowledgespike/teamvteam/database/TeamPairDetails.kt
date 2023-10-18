package com.knowledgespike.teamvteam.database

import com.knowledgespike.teamvteam.daos.*

data class TeamPairDetails(val teams: Array<String>,  val matchDto: MatchDto) {

    val strikeRateScoreLimit = 20
    val strikeRateLowerBallsLimit = 10
    val strikeRateUpperBallsLimit = 20
    val economyRateOversLimit = 3

    val highestScores = mutableListOf<MutableList<TotalDto>>(mutableListOf(), mutableListOf())
    val highestIndividualScore = arrayListOf<MutableList<HighestScoreDto>>(mutableListOf(), mutableListOf())
    val highestIndividualStrikeRates = arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
    val highestIndividualStrikeRatesWithLimit = arrayListOf<MutableList<StrikeRateDto>>(mutableListOf(), mutableListOf())
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

    fun addTeamData(teamRecords: TeamRecords, teamParamA: TeamParams, teamParamB: TeamParams, matchType: String) {
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                teamParamA, matchType
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                teamParamB, matchType
            )
        )

        var lowestAllOutTotals = teamRecords.getLowestAllOutTotals(teamParamA)
        var lowestCompleteTotals = teamRecords.getLowestCompleteTotals(teamParamA)
        var lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(teamParamA)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))

        lowestAllOutTotals = teamRecords.getLowestAllOutTotals(teamParamB)
        lowestCompleteTotals = teamRecords.getLowestCompleteTotals(teamParamB)
        lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(teamParamB)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))
    }

    fun addIndividualData(teamRecords: TeamRecords, teamParamA: TeamParams, teamParamB: TeamParams, matchType: String) {

        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(teamParamA, matchType)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(teamParamB, matchType)
        )


        if (nonFcMatchTypes.contains(matchType)) {
            highestIndividualStrikeRates[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(teamParamA)
            )
            highestIndividualStrikeRates[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(teamParamB)
            )

            highestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(teamParamA, strikeRateScoreLimit)
            )
            highestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(teamParamB, strikeRateScoreLimit)
            )

            lowestIndividualStrikeRates[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(teamParamA, strikeRateLowerBallsLimit)
            )
            lowestIndividualStrikeRates[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(teamParamB, strikeRateLowerBallsLimit)
            )

            lowestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(teamParamA, strikeRateUpperBallsLimit)
            )
            lowestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(teamParamB, strikeRateUpperBallsLimit)
            )


            mostSixes[0].addAll(
                teamRecords.getHighestIndividualSixes(teamParamA)
            )
            mostSixes[1].addAll(
                teamRecords.getHighestIndividualSixes(teamParamB)
            )

            mostFours[0].addAll(
                teamRecords.getHighestIndividualFours(teamParamA)
            )
            mostFours[1].addAll(
                teamRecords.getHighestIndividualFours(teamParamB)
            )

            mostBoundaries[0].addAll(
                teamRecords.getHighestIndividualBoundaries(teamParamA)
            )
            mostBoundaries[1].addAll(
                teamRecords.getHighestIndividualBoundaries(teamParamB)
            )
        }

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


        bestFoW[0].putAll(
            teamRecords.getHighestFoW(teamParamA)
        )

        bestFoW[1].putAll(
            teamRecords.getHighestFoW(teamParamB)
        )

        if (nonFcMatchTypes.contains(matchType)) {
            bestBowlingSRInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(teamParamA)
            )
            bestBowlingSRInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(teamParamB)
            )

            bestBowlingSRWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(teamParamA, economyRateOversLimit)
            )
            bestBowlingSRWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(teamParamB, economyRateOversLimit)
            )

            bestBowlingERInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(teamParamA)
            )
            bestBowlingERInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(teamParamB)
            )

            bestBowlingERWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(teamParamA, economyRateOversLimit)
            )
            bestBowlingERWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(teamParamB, economyRateOversLimit)
            )

            worstBowlingERInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(teamParamA)
            )
            worstBowlingERInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(teamParamB)
            )

            worstBowlingERWithLimitInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(teamParamA, economyRateOversLimit)
            )
            worstBowlingERWithLimitInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(teamParamB, economyRateOversLimit)
            )
        }

        mostRunsVsOpposition[0].addAll(
            teamRecords.getMostRuns(teamParamA)
        )


        mostRunsVsOpposition[1].addAll(
            teamRecords.getMostRuns(teamParamB)
        )

        mostWicketsVsOpposition[0].addAll(
            teamRecords.getMostWickets(teamParamA)
        )
        mostWicketsVsOpposition[1].addAll(
            teamRecords.getMostWickets(teamParamB)
        )

        mostCatchesVsOpposition[0].addAll(
            teamRecords.getMostCatches(teamParamA)
        )
        mostCatchesVsOpposition[1].addAll(
            teamRecords.getMostCatches(teamParamB)
        )

        mostStumpingsVsOpposition[0].addAll(
            teamRecords.getMostStumpings(teamParamA)
        )
        mostStumpingsVsOpposition[1].addAll(
            teamRecords.getMostStumpings(teamParamB)
        )

    }

}