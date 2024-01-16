package com.knowledgespike.progressive.json

import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.shared.data.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.Path

@Serializable
data class ProgressiveData(
    val team1: String,
    val team2: String,
    val competitionTitle: String,
    val competitionSubType: String,
    val matchDto: MatchDto,
    val authors: List<String>,

    val highestScores: List<List<TotalDto>>,
    val lowestAllOutScores: List<List<TotalDto>>,
    val highestIndividualScores: List<List<HighestScoreDto>>,
    val bestBowlingInnings: List<List<BestBowlingDto>>,
    val bestBowlingMatch: List<List<BestBowlingDto>>,
    val bestFoW: List<Map<Int, FowDetails>>,
    val lastUpdated: Instant
)

fun getProgressiveJsonData(jsonDirectory: String, fileName: String): ProgressiveData? {
    return getProgressiveJsonData("${jsonDirectory}/$fileName")
}

fun getProgressiveJsonData(fileName: String): ProgressiveData? {
    val file: File = Path(fileName).toFile()

    if (!file.exists())
        return null

    val data: String
    data = file.readText()

    val details: ProgressiveData = Json.decodeFromString(data)
    return details
}

fun writeJsonMatchData(baseDirectory: String, fileName: String, data: ProgressiveData) {
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
