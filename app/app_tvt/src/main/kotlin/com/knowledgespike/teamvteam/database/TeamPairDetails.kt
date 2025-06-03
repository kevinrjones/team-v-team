package com.knowledgespike.teamvteam.database

import com.knowledgespike.shared.data.*
import com.knowledgespike.teamvteam.daos.*
import org.jooq.SQLDialect
import java.sql.Connection

data class TeamPairDetails(val teams: Array<String>, val matchDto: MatchDto, private val nameUpdates:  List<NameUpdate>) {

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

    /**
     * Adds team data including highest scores, lowest scores, and other aggregated statistics
     * for the specified teams and parameters.
     *
     * @param connection Database connection to execute queries.
     * @param dialect SQL dialect to adapt queries to the specific SQL database implementation.
     * @param countryIds List of country IDs associated with the teams.
     * @param teamParamA Parameters for the first team including IDs, opponents, and match types.
     * @param teamParamB Parameters for the second team including IDs, opponents, and match types.
     * @param startFrom The starting point (timestamp or ID) for filtering relevant data.
     */
    fun addTeamData(
        connection: Connection,
        dialect: SQLDialect,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams
    ) {
        val teamRecords = TeamRecords(connection, dialect, nameUpdates)
        highestScores[0].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamA,
                matchDto.matchIds
            )
        )

        highestScores[1].addAll(
            teamRecords.getHighestTotals(
                countryIds,
                teamParamB,
                matchDto.matchIds
            )
        )

        var lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamA, matchDto.matchIds)
        var lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamA, matchDto.matchIds)
        var lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamA, matchDto.matchIds)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))

        lowestAllOutTotals = teamRecords.getLowestAllOutTotals(countryIds, teamParamB, matchDto.matchIds)
        lowestCompleteTotals = teamRecords.getLowestCompleteTotals(countryIds, teamParamB, matchDto.matchIds)
        lowestIncompleteTotals = teamRecords.getLowestIncompleteTotals(countryIds, teamParamB, matchDto.matchIds)
        teamAllLowestScores.add(LowestScoreDto(lowestAllOutTotals, lowestCompleteTotals, lowestIncompleteTotals))
    }

    fun addIndividualData(
        connection: Connection,
        dialect: SQLDialect,
        countryIds: List<Int>,
        teamParamA: TeamParams,
        teamParamB: TeamParams,
        matchType: String,
        matchIds: List<Int>
    ) {

        val teamRecords = TeamRecords(connection, dialect, nameUpdates)
        highestIndividualScore[0].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamA,  matchIds)
        )

        highestIndividualScore[1].addAll(
            teamRecords.getHighestIndividualScores(countryIds, teamParamB,  matchIds)
        )


        if (nonFcMatchTypes.contains(matchType)) {
            highestIndividualStrikeRates[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA,  matchIds =matchIds)
            )
            highestIndividualStrikeRates[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB,  matchIds =matchIds)
            )

            highestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamA,  strikeRateScoreLimit, matchIds)
            )
            highestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getHighestIndividualStrikeRate(countryIds, teamParamB,  strikeRateScoreLimit, matchIds)
            )

            lowestIndividualStrikeRates[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateLowerBallsLimit,  matchIds)
            )
            lowestIndividualStrikeRates[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateLowerBallsLimit,  matchIds)
            )

            lowestIndividualStrikeRatesWithLimit[0].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamA, strikeRateUpperBallsLimit,  matchIds)
            )
            lowestIndividualStrikeRatesWithLimit[1].addAll(
                teamRecords.getLowestIndividualStrikeRate(countryIds, teamParamB, strikeRateUpperBallsLimit,  matchIds)
            )


            mostSixes[0].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamA,  matchIds)
            )
            mostSixes[1].addAll(
                teamRecords.getHighestIndividualSixes(countryIds, teamParamB,  matchIds)
            )

            mostFours[0].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamA,  matchIds)
            )
            mostFours[1].addAll(
                teamRecords.getHighestIndividualFours(countryIds, teamParamB,  matchIds)
            )

            mostBoundaries[0].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamA,  matchIds)
            )
            mostBoundaries[1].addAll(
                teamRecords.getHighestIndividualBoundaries(countryIds, teamParamB,  matchIds)
            )
        }

        bestBowlingInnings[0].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamA,  matchIds)
        )

        bestBowlingInnings[1].addAll(
            teamRecords.getBestBowlingInnings(countryIds, teamParamB,  matchIds)
        )

        bestBowlingMatch[0].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamA,  matchIds)
        )

        bestBowlingMatch[1].addAll(
            teamRecords.getBestBowlingMatch(countryIds, teamParamB,  matchIds)
        )


        bestFoW[0].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamA,  matchIds)
        )

        bestFoW[1].putAll(
            teamRecords.getHighestFoW(countryIds, teamParamB,  matchIds)
        )

        if (nonFcMatchTypes.contains(matchType)) {
            bestBowlingSRInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA,  matchIds)
            )
            bestBowlingSRInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB,  matchIds)
            )

            bestBowlingSRWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamA,  matchIds, economyRateOversLimit)
            )
            bestBowlingSRWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingStrikeRate(countryIds, teamParamB,  matchIds, economyRateOversLimit)
            )

            bestBowlingERInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA,  matchIds)
            )
            bestBowlingERInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB,  matchIds)
            )

            bestBowlingERWithLimitInnings[0].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamA,  matchIds, economyRateOversLimit)
            )
            bestBowlingERWithLimitInnings[1].addAll(
                teamRecords.getBestBowlingEconRate(countryIds, teamParamB,  matchIds, economyRateOversLimit)
            )

            worstBowlingERInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA,  matchIds)
            )
            worstBowlingERInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB,  matchIds)
            )

            worstBowlingERWithLimitInnings[0].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamA,  matchIds, economyRateOversLimit)
            )
            worstBowlingERWithLimitInnings[1].addAll(
                teamRecords.getWorstBowlingEconRate(countryIds, teamParamB,  matchIds, economyRateOversLimit)
            )
        }

        mostRunsVsOpposition[0].addAll(
            teamRecords.getMostRuns(countryIds, teamParamA,  matchIds)
        )


        mostRunsVsOpposition[1].addAll(
            teamRecords.getMostRuns(countryIds, teamParamB,  matchIds)
        )

        mostWicketsVsOpposition[0].addAll(
            teamRecords.getMostWickets(countryIds, teamParamA,  matchIds)
        )
        mostWicketsVsOpposition[1].addAll(
            teamRecords.getMostWickets(countryIds, teamParamB,  matchIds)
        )

        mostCatchesVsOpposition[0].addAll(
            teamRecords.getMostCatches(countryIds, teamParamA,  matchIds)
        )
        mostCatchesVsOpposition[1].addAll(
            teamRecords.getMostCatches(countryIds, teamParamB,  matchIds)
        )

        mostStumpingsVsOpposition[0].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamA,  matchIds)
        )
        mostStumpingsVsOpposition[1].addAll(
            teamRecords.getMostStumpings(countryIds, teamParamB,  matchIds)
        )

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TeamPairDetails

        if (strikeRateScoreLimit != other.strikeRateScoreLimit) return false
        if (strikeRateLowerBallsLimit != other.strikeRateLowerBallsLimit) return false
        if (strikeRateUpperBallsLimit != other.strikeRateUpperBallsLimit) return false
        if (economyRateOversLimit != other.economyRateOversLimit) return false
        if (!teams.contentEquals(other.teams)) return false
        if (matchDto != other.matchDto) return false
        if (authors != other.authors) return false
        if (highestScores != other.highestScores) return false
        if (highestIndividualScore != other.highestIndividualScore) return false
        if (highestIndividualStrikeRates != other.highestIndividualStrikeRates) return false
        if (highestIndividualStrikeRatesWithLimit != other.highestIndividualStrikeRatesWithLimit) return false
        if (lowestIndividualStrikeRates != other.lowestIndividualStrikeRates) return false
        if (lowestIndividualStrikeRatesWithLimit != other.lowestIndividualStrikeRatesWithLimit) return false
        if (mostFours != other.mostFours) return false
        if (mostSixes != other.mostSixes) return false
        if (mostBoundaries != other.mostBoundaries) return false
        if (bestBowlingInnings != other.bestBowlingInnings) return false
        if (bestBowlingMatch != other.bestBowlingMatch) return false
        if (bestBowlingSRInnings != other.bestBowlingSRInnings) return false
        if (bestBowlingSRWithLimitInnings != other.bestBowlingSRWithLimitInnings) return false
        if (bestBowlingERInnings != other.bestBowlingERInnings) return false
        if (bestBowlingERWithLimitInnings != other.bestBowlingERWithLimitInnings) return false
        if (worstBowlingERInnings != other.worstBowlingERInnings) return false
        if (worstBowlingERWithLimitInnings != other.worstBowlingERWithLimitInnings) return false
        if (bestFoW != other.bestFoW) return false
        if (mostRunsVsOpposition != other.mostRunsVsOpposition) return false
        if (mostWicketsVsOpposition != other.mostWicketsVsOpposition) return false
        if (mostCatchesVsOpposition != other.mostCatchesVsOpposition) return false
        if (mostStumpingsVsOpposition != other.mostStumpingsVsOpposition) return false
        if (teamAllLowestScores != other.teamAllLowestScores) return false
        if (nonFcMatchTypes != other.nonFcMatchTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = strikeRateScoreLimit
        result = 31 * result + strikeRateLowerBallsLimit
        result = 31 * result + strikeRateUpperBallsLimit
        result = 31 * result + economyRateOversLimit
        result = 31 * result + teams.contentHashCode()
        result = 31 * result + matchDto.hashCode()
        result = 31 * result + authors.hashCode()
        result = 31 * result + highestScores.hashCode()
        result = 31 * result + highestIndividualScore.hashCode()
        result = 31 * result + highestIndividualStrikeRates.hashCode()
        result = 31 * result + highestIndividualStrikeRatesWithLimit.hashCode()
        result = 31 * result + lowestIndividualStrikeRates.hashCode()
        result = 31 * result + lowestIndividualStrikeRatesWithLimit.hashCode()
        result = 31 * result + mostFours.hashCode()
        result = 31 * result + mostSixes.hashCode()
        result = 31 * result + mostBoundaries.hashCode()
        result = 31 * result + bestBowlingInnings.hashCode()
        result = 31 * result + bestBowlingMatch.hashCode()
        result = 31 * result + bestBowlingSRInnings.hashCode()
        result = 31 * result + bestBowlingSRWithLimitInnings.hashCode()
        result = 31 * result + bestBowlingERInnings.hashCode()
        result = 31 * result + bestBowlingERWithLimitInnings.hashCode()
        result = 31 * result + worstBowlingERInnings.hashCode()
        result = 31 * result + worstBowlingERWithLimitInnings.hashCode()
        result = 31 * result + bestFoW.hashCode()
        result = 31 * result + mostRunsVsOpposition.hashCode()
        result = 31 * result + mostWicketsVsOpposition.hashCode()
        result = 31 * result + mostCatchesVsOpposition.hashCode()
        result = 31 * result + mostStumpingsVsOpposition.hashCode()
        result = 31 * result + teamAllLowestScores.hashCode()
        result = 31 * result + nonFcMatchTypes.hashCode()
        return result
    }

}