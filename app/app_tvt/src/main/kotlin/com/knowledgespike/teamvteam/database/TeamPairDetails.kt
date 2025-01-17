package com.knowledgespike.teamvteam.database

import com.knowledgespike.shared.data.*
import com.knowledgespike.teamvteam.daos.*
import org.jooq.SQLDialect
import java.sql.Connection

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
        connection: java.sql.Connection,
        dialect: SQLDialect,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        startFrom: Long
    ) {
        val teamRecords = TeamRecords(connection, dialect)
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamA,
                startFrom
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamB,
                startFrom
            )
        )

        var lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamA,startFrom)
        var lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamA, startFrom)
        var lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamA, startFrom)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))

        lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamB, startFrom)
        lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamB, startFrom)
        lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamB, startFrom)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))
    }

    fun addIndividualData(
        connection: Connection,
        dialect: SQLDialect,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        matchType: String,
        startFrom: Long
    ) {

        val teamRecords = TeamRecords(connection, dialect)
        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamA, startFrom)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamB, startFrom)
        )


        if (nonFcMatchTypes.contains(matchType)) {
            highestIndividualStrikeRates[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA, startFrom)
            )
            highestIndividualStrikeRates[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB, startFrom)
            )

            highestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA, startFrom, strikeRateScoreLimit)
            )
            highestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB, startFrom, strikeRateScoreLimit)
            )

            lowestIndividualStrikeRates[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateLowerBallsLimit, startFrom)
            )
            lowestIndividualStrikeRates[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateLowerBallsLimit, startFrom)
            )

            lowestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateUpperBallsLimit, startFrom)
            )
            lowestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateUpperBallsLimit, startFrom)
            )


            mostSixes[0].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamA, startFrom)
            )
            mostSixes[1].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamB, startFrom)
            )

            mostFours[0].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamA, startFrom)
            )
            mostFours[1].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamB, startFrom)
            )

            mostBoundaries[0].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamA, startFrom)
            )
            mostBoundaries[1].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamB, startFrom)
            )
        }

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamA, startFrom)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamB, startFrom)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamA, startFrom)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamB, startFrom)
        )


        bestFoW[0].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamA, startFrom)
        )

        bestFoW[1].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamB, startFrom)
        )

        if (nonFcMatchTypes.contains(matchType)) {
            bestBowlingSRInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA, startFrom)
            )
            bestBowlingSRInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB, startFrom)
            )

            bestBowlingSRWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA, startFrom, economyRateOversLimit)
            )
            bestBowlingSRWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB, startFrom, economyRateOversLimit)
            )

            bestBowlingERInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA, startFrom)
            )
            bestBowlingERInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB, startFrom)
            )

            bestBowlingERWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA, startFrom, economyRateOversLimit)
            )
            bestBowlingERWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB, startFrom, economyRateOversLimit)
            )

            worstBowlingERInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA, startFrom)
            )
            worstBowlingERInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB, startFrom)
            )

            worstBowlingERWithLimitInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA, startFrom, economyRateOversLimit)
            )
            worstBowlingERWithLimitInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB, startFrom, economyRateOversLimit)
            )
        }

        mostRunsVsOpposition[0].addAll(
            teamRecords.getMostRuns(countryIds, teamParamA, startFrom)
        )


        mostRunsVsOpposition[1].addAll(
            teamRecords.getMostRuns(countryIds, teamParamB, startFrom)
        )

        mostWicketsVsOpposition[0].addAll(
            teamRecords.getMostWickets(countryIds, teamParamA, startFrom)
        )
        mostWicketsVsOpposition[1].addAll(
            teamRecords.getMostWickets(countryIds, teamParamB, startFrom)
        )

        mostCatchesVsOpposition[0].addAll(
            teamRecords.getMostCatches(countryIds, teamParamA, startFrom)
        )
        mostCatchesVsOpposition[1].addAll(
            teamRecords.getMostCatches(countryIds, teamParamB, startFrom)
        )

        mostStumpingsVsOpposition[0].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamA, startFrom)
        )
        mostStumpingsVsOpposition[1].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamB, startFrom)
        )

    }

}