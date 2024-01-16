package com.knowledgespike.shared.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


@Serializable
data class MatchDto(val count:  Int, val startDate: LocalDateTime, val endDate: LocalDateTime)

@Serializable
data class FoWDto(
    val team: String,
    val opponents: String,
    val location: String,
    val seriesDate: String,
    val partnership: Int,
    val wicket: Int,
    val undefeated: Boolean,
    val player1Name: String,
    val player1Score: Int,
    val player1NotOut: Boolean,
    val player1Position: Int,
    val player2Name: String,
    val player2Score: Int,
    val player2NotOut: Boolean,
    val player2Position: Int,
)

@Serializable
data class MultiPlayerFowDto(val total: Int, val wicket: Int, val playerDetails: List<FoWDto>)


@Serializable
data class FowDetails(val standardFow: List<FoWDto>, val multiPlayerFow: List<MultiPlayerFowDto>)

@Serializable
data class TeamPairHomePagesJson(
    val mainTeamName: String,
    val teamNames: List<Pair<String, String>>,
    val matchDesignator: String,
    val matchType: String
)

@Serializable
data class CompetitionIndexPage(
    val teamNames: List<String>,
    val matchSubType: String,
    val country: String,
    val gender: String,
    val title: String,
    val extraMessages: List<String>
)

@Serializable
data class TotalDto(
    val team: String,
    val opponents: String,
    val total: Int,
    val wickets: Int,
    val declared: Boolean,
    val location: String,
    val seriesDate: String
)

@Serializable
data class HighestScoreDto(
    val name: String,
    val team: String,
    val opponents: String,
    val score: Int,
    val notOut: Boolean,
    val location: String,
    val seriesDate: String,
)


fun generateIndexPageData(
    teamNames: List<String>,
    matchSubType: String,
    country: String,
    gender: String,
    title: String,
    extraMessages: List<String>,
    jsonDirectory: String
) {
    if (teamNames.isNotEmpty()) {
        val fileName =
            "${jsonDirectory}/index.json"

        val file = File(fileName)
        file.parentFile.mkdirs()

        writeJsonTeamPairPageIndexData(
            fileName,
            CompetitionIndexPage(teamNames, matchSubType, country, gender, title, extraMessages)
        )
    }
}

fun writeJsonTeamPairPageIndexData(fileName: String, data: CompetitionIndexPage) {

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

