package com.knowledgespike.teamvteam.database

import com.knowledgespike.extensions.generateFileName
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.shared.database.checkIfShouldProcess
import com.knowledgespike.shared.database.getCountOfMatchesBetweenTeams
import com.knowledgespike.shared.database.getCountryIdsFromName
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.Application.Companion.dialect
import com.knowledgespike.teamvteam.json.getTvTJsonData


class ProcessTeams(
    allTeams: TeamNameToIds,
    opponentsForTeam: Map<String, TeamNameToIds>,
    private val opponentsWithAuthors: Map<String, List<Author>>
) {
    val log by LoggerDelegate()

    private val idPairs: List<TeamsAndOpponents>

    init {
        idPairs = buildPairsOfTeamsThatMayCompete(allTeams, opponentsForTeam)
    }


    fun process(
        databaseConnection: DatabaseConnection,
        countries: List<String>,
        matchSubType: String,
        jsonDirectory: String,
        competitionTeams: List<String>,
        callback: (teamPairDetails: TeamPairDetails, jsonDirectory: String) -> Unit
    ): Map<String, TeamPairHomePagesData> {

        val matchType: String = matchTypeFromSubType(matchSubType)

        val countryIds = getCountryIdsFromName(countries, databaseConnection)

        var pairsForPage: Map<String, TeamPairHomePagesData> = mutableMapOf()

        for (teamsAndOpponents in idPairs) {

            val matchDto =
                getCountOfMatchesBetweenTeams(
                    databaseConnection,
                    countryIds,
                    teamsAndOpponents,
                    matchSubType,
                    dialect
                )
            if (matchDto.count != 0) {
                val teamPairDetails =
                    TeamPairDetails(
                        arrayOf(teamsAndOpponents.teamName, teamsAndOpponents.opponentsName),
                        matchDto
                    )

                val fileName = teamPairDetails.generateFileName(matchSubType)

                val lastUpdatedDate = getLastUpdatedDate(jsonDirectory, fileName)

                pairsForPage = addPairToPage(
                    competitionTeams,
                    teamsAndOpponents.teamName,
                    teamsAndOpponents.opponentsName,
                    pairsForPage
                )


                if (lastUpdatedDate == null || checkIfShouldProcess(
                        databaseConnection,
                        teamsAndOpponents.teamIds,
                        teamsAndOpponents.opponentIds,
                        matchType,
                        lastUpdatedDate,
                        dialect
                    )
                ) {

                    val teamParams = getTeamParams(teamsAndOpponents, matchType, matchSubType)
                    log.info("About to process {}", teamParams)
                    teamPairDetails.addTeamData(databaseConnection, countryIds, teamParams.first, teamParams.second)
                    teamPairDetails.addIndividualData(
                        databaseConnection,
                        countryIds,
                        teamParams.first,
                        teamParams.second,
                        matchType
                    )

                    val authors1 = opponentsWithAuthors
                        .filter { it.key == teamPairDetails.teams[0] }
                        .get(teamPairDetails.teams[0])?.map { it }
                        ?.filter { it.opponent == teamPairDetails.teams[1] }
                        ?.map { it.name }
                        ?: listOf()

                    val authors2 = opponentsWithAuthors
                        .filter { it.key == teamPairDetails.teams[1] }
                        .get(teamPairDetails.teams[1])?.map { it }
                        ?.filter { it.opponent == teamPairDetails.teams[0] }
                        ?.map { it.name }
                        ?: listOf()


                    teamPairDetails.authors.addAll(authors1 + authors2)
                    val tempAuthors = teamPairDetails.authors.distinct()
                    teamPairDetails.authors.clear()
                    teamPairDetails.authors.addAll(tempAuthors)

                    callback(teamPairDetails, jsonDirectory)
                }
            }
        }
        return pairsForPage
    }

    private fun getLastUpdatedDate(jsonDirectory: String, fileName: String): Long? {
        val details = getTvTJsonData(jsonDirectory, fileName)
        return details?.lastUpdated?.epochSeconds
    }


}

