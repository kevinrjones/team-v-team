package com.knowledgespike.teamvteam.json

import com.knowledgespike.teamvteam.daos.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun writeJsonMatchData(baseDirectory: String, fileName: String, data: TeamPairDetailsData) {
    val fqnName = "$baseDirectory/$fileName"

    val format = Json {
        prettyPrint = true;
        encodeDefaults = true
    }
    val formattedData: String = format.encodeToString(data)


    val file = File(fqnName)
    file.parentFile.mkdirs()

    file.createNewFile()


    file.writeText(formattedData)
}

fun writeJsonTeamPairPageData(fileName: String, data: TeamPairHomePagesJson) {

    val format = Json {
        prettyPrint = true;
        encodeDefaults = true
    }
    val formattedData: String = format.encodeToString(data)


    val file = File(fileName)
    file.parentFile.mkdirs()

    file.createNewFile()


    file.writeText(formattedData)
}

@Serializable
data class TeamPairDetailsData(
    val team1: String,
    val team2: String,
    val competitionTitle: String,
    val competitionSubType: String,
    val matchDto: MatchDto,
    val authors: List<String>,

    val highestScores: List<List<TotalDto>>,
    val highestIndividualScore: List<List<HighestScoreDto>>,
    val highestIndividualStrikeRates: List<List<StrikeRateDto>>,
    val highestIndividualStrikeRatesWithLimit: List<List<StrikeRateDto>>,
    val lowestIndividualStrikeRates: List<List<StrikeRateDto>>,
    val lowestIndividualStrikeRatesWithLimit: List<List<StrikeRateDto>>,
    val mostFours: List<List<BoundariesDto>>,
    val mostSixes: List<List<BoundariesDto>>,
    val mostBoundaries: List<List<BoundariesDto>>,

    val bestBowlingInnings: List<List<BestBowlingDto>>,
    val bestBowlingMatch: List<List<BestBowlingDto>>,

    val bestBowlingSRInnings: List<List<BowlingRatesDto>>,
    val bestBowlingSRWithLimitInnings: List<List<BowlingRatesDto>>,
    val bestBowlingERInnings: List<List<BowlingRatesDto>>,
    val bestBowlingERWithLimitInnings: List<List<BowlingRatesDto>>,
    val worstBowlingERInnings: List<List<BowlingRatesDto>>,
    val worstBowlingERWithLimitInnings: List<List<BowlingRatesDto>>,

    val bestFoW: List<Map<Int, FowDetails>>,

    val mostRunsVsOpposition: List<List<MostRunsDto>>,
    val mostWicketsVsOpposition: List<List<MostWicketsDto>>,
    val mostCatchesVsOpposition: List<List<MostDismissalsDto>>,
    val mostStumpingsVsOpposition: List<List<MostDismissalsDto>>,

    val teamAllLowestScores: List<LowestScoreDto>,
    val lastUpdated: Instant,
    val strikeRateScoreLimit: Int = 20,
    val strikeRateLowerBallsLimit: Int = 10,
    val strikeRateUpperBallsLimit: Int = 20,
    val economyRateOversLimit: Int = 3

)

@Serializable
data class TeamPairHomePagesJson(val mainTeamName: String, val teamNames: List<Pair<String, String>>, val matchDesignator: String, val matchType: String)
