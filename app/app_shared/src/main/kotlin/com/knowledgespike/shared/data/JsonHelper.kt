package com.knowledgespike.shared.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import kotlin.io.path.Path

fun createTeamPairHomePagesData(
    matchSubType: String,
    title: String,
    newPairsForPage: Map<String, TeamPairHomePagesData>,
    jsonDirectory: String
): MutableList<String> {
    val teamNames = mutableListOf<String>()
    newPairsForPage.filter { it.value.shouldHaveOwnPage }.forEach { (teamName, teamPairHomePagesData) ->

        val fileName =
            "${jsonDirectory}/${teamName.replace(" ", "_")}_${matchSubType}.json"
        val file = File(fileName)
        file.parentFile.mkdirs()

        val names = teamPairHomePagesData.teamPairDetails.map { it }
        val teamPairHomePagesDataJson = TeamPairHomePagesJson(teamName, names, title, matchSubType)

        writeJsonTeamPairPageData(fileName, teamPairHomePagesDataJson)
        teamNames.add(teamName)

    }

    return teamNames
}

fun getHomePageJsonData(fileName: String): TeamPairHomePagesJson? {
    val file: File = Path(fileName).toFile()

    if (!file.exists())
        return null

    val data: String
    data = file.readText()

    val details: TeamPairHomePagesJson = Json.decodeFromString(data)
    return details
}

fun getIndexPageJsonData(fileName: String): CompetitionIndexPage? {
    val file: File = Path(fileName).toFile()

    if (!file.exists())
        return null

    val data: String
    data = file.readText()

    val details: CompetitionIndexPage = Json.decodeFromString(data)
    return details
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


fun generateIndexPageData(
    teamNames: List<String>,
    matchSubType: String,
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
            CompetitionIndexPage(teamNames, matchSubType, gender, title, extraMessages)
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

fun getCapitalizedCountryName(country: String): String {
    return when (val capitalizedCountry =
        country.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) {
        "England" -> "in $capitalizedCountry"
        "Australia" -> "in $capitalizedCountry"
        "South Africa" -> "in $capitalizedCountry"
        "New Zealand" -> "in $capitalizedCountry"
        "West Indies" -> "in $capitalizedCountry"
        "India" -> "in $capitalizedCountry"
        "Pakistan" -> "in $capitalizedCountry"
        "Bangladesh" -> "in $capitalizedCountry"
        "Sri Lanka" -> "in $capitalizedCountry"
        "Afghanistan" -> "in $capitalizedCountry"
        "Ireland" -> "in $capitalizedCountry"
        else -> ""
    }
}
