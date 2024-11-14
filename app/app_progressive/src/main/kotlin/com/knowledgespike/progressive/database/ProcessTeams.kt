package com.knowledgespike.progressive.database

import com.knowledgespike.progressive.json.getProgressiveJsonData
import com.knowledgespike.shared.data.TeamPairHomePagesData
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnectionDetails
import com.knowledgespike.shared.database.checkIfShouldProcess
import com.knowledgespike.shared.database.getCountOfMatchesBetweenTeams
import com.knowledgespike.shared.database.getCountryIdsFromName
import com.knowledgespike.shared.logging.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jooq.SQLDialect
import java.sql.DriverManager

class ProcessTeams(
    allTeams: TeamNameToValidTeam,
    opponentsForTeam: Map<String, TeamNameToValidTeam>,
    private val opponentsWithAuthors: Map<String, List<Author>>,
    private val dialect: SQLDialect,
) {

    private val log by LoggerDelegate()

    private val idPairs: List<TeamsAndOpponents> = buildPairsOfTeamsThatMayCompete(allTeams, opponentsForTeam)

    private val teamAndAllOpponents = buildPairsOfTeamsOpponents(allTeams, opponentsForTeam)

    suspend fun processTeamPairs(
        databaseConnectionDetails: DatabaseConnectionDetails,
        countries: List<String>,
        matchSubType: String,
        jsonDirectory: String,
        competitionTeams: List<String>,
        overall: Boolean,
        callback: (teamPairDetails: TeamPairDetails, jsonDirectory: String) -> Unit,
    ): Map<String, TeamPairHomePagesData> {
        val matchType: String = matchTypeFromSubType(matchSubType)

        var pairsForPage: Map<String, TeamPairHomePagesData> = mutableMapOf()

        val countryIds = getCountryIdsFromName(countries, databaseConnectionDetails)


        for (teamsAndOpponents in idPairs) {
            log.debug("Start processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)
            DriverManager.getConnection(
                databaseConnectionDetails.connectionString,
                databaseConnectionDetails.userName,
                databaseConnectionDetails.password
            ).use { connection ->

                val matchDto =
                    getCountOfMatchesBetweenTeams(
                        connection,
                        databaseConnectionDetails,
                        countryIds,
                        teamsAndOpponents,
                        matchSubType,
                        overall,
                        teamsAndOpponents.startFrom
                    )
                if (matchDto.count + matchDto.abandoned + matchDto.cancelled != 0) {
                    log.debug("Processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)
                    val teamPairDetails =
                        TeamPairDetails(
                            arrayOf(teamsAndOpponents.teamName, teamsAndOpponents.opponentsName),
                            matchDto,
                            teamsAndOpponents.startFrom
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
                            databaseConnectionDetails,
                            teamsAndOpponents.teamIds,
                            teamsAndOpponents.opponentIds,
                            matchType,
                            lastUpdatedDate,
                            dialect
                        )
                    ) {
                        withContext(Dispatchers.IO) {
                            val job = launch {
                                val teamParams = getTeamParams(teamsAndOpponents, matchType, matchSubType)
                                teamPairDetails.getFallOfWicketRecords(
                                    databaseConnectionDetails,
                                    teamParams.first,
                                    teamParams.second,
                                    overall
                                )
                                teamPairDetails.getTeamRecords(
                                    databaseConnectionDetails,
                                    teamParams.first,
                                    teamParams.second
                                )
                                teamPairDetails.getIndividualRecords(
                                    databaseConnectionDetails,
                                    teamParams.first,
                                    teamParams.second
                                )


                                val maybeAuthors =
                                    getAuthors(teamPairDetails, teamPairDetails.teams[0], teamPairDetails.teams[1]) +
                                            getAuthors(
                                                teamPairDetails,
                                                teamPairDetails.teams[1],
                                                teamPairDetails.teams[0]
                                            )

                                teamPairDetails.authors.addAll(maybeAuthors)
                                val tempAuthors = teamPairDetails.authors.distinct()
                                teamPairDetails.authors.clear()
                                teamPairDetails.authors.addAll(tempAuthors)

                            }

                            job.join()

                            callback(teamPairDetails, jsonDirectory)
                        }
                    }
                }
            }
        }
        return pairsForPage
    }

    suspend fun processTeamVsAllOpponents(
        connectionDetails: DatabaseConnectionDetails,
        country: List<String>,
        matchSubType: String,
        jsonDirectory: String,
        overall: Boolean,
        callback: (teamdAndOpponents: TeamAndAllOpponentsDetails, jsonDirectory: String) -> Unit,
    ) {
        val matchType: String = matchTypeFromSubType(matchSubType)

        val countryId = getCountryIdsFromName(country, connectionDetails)

        for ((teamAndIds, opponents) in teamAndAllOpponents) {
            DriverManager.getConnection(
                connectionDetails.connectionString,
                connectionDetails.userName,
                connectionDetails.password
            ).use { connection ->
                val matchDto =
                    getCountOfMatchesBetweenTeams(
                        connection,
                        connectionDetails,
                        countryId,
                        TeamsAndOpponents(
                            teamAndIds.teamName,
                            teamAndIds.teamIds,
                            "all",
                            opponents,
                            teamAndIds.startFrom
                        ),
                        matchSubType,
                        overall,
                        teamAndIds.startFrom,
                    )

                val teamAndAllOpponentsDetails = TeamAndAllOpponentsDetails(teamAndIds.teamName, matchDto)

                withContext(Dispatchers.IO) {
                    val job = launch {

                        teamAndAllOpponentsDetails.getFallOfWicketRecords(
                            connectionDetails,
                            teamAndIds,
                            opponents,
                            matchType,
                            matchSubType,
                            overall,
                            teamAndIds.startFrom,
                        )

                        teamAndAllOpponentsDetails.getTeamRecords(
                            connectionDetails,
                            teamAndIds,
                            opponents,
                            matchType,
                            matchSubType,
                            overall,
                            teamAndIds.startFrom,
                        )

                        teamAndAllOpponentsDetails.getIndividualRecords(
                            connectionDetails,
                            teamAndIds,
                            opponents,
                            matchType,
                            matchSubType,
                            overall,
                            teamAndIds.startFrom,
                        )

                        val maybeAuthors =
                            opponentsWithAuthors.flatMap { it.value }.map { it.name }.toSet().toList()
                        teamAndAllOpponentsDetails.authors.addAll(maybeAuthors)

                    }
                    job.join()

                    callback(teamAndAllOpponentsDetails, jsonDirectory)
                }
            }
        }
    }


    private fun getAuthors(teamPairDetails: TeamPairDetails, author1: String, author2: String): List<String> {
        val maybeAuthors = opponentsWithAuthors
            .filter { it.key == author1 }
            .get(teamPairDetails.teams[0])?.map { it }
            ?.filter { it.opponent == author2 }
            ?.map { it.name }
        return maybeAuthors ?: listOf()
    }
}


fun TeamPairDetails.generateFileName(
    matchSubType: String,
): String {
    val fileName = "${this.teams[0].replace(" ", "_")}_v_${
        this.teams[1].replace(" ", "_")
    }_${matchSubType}.json"
    return fileName
}

fun TeamAndAllOpponentsDetails.generateFileName(
    matchSubType: String,
): String {
    val fileName = "${this.teamName.replace(" ", "_")}_v_all_${matchSubType}.json"
    return fileName

}


fun getLastUpdatedDate(jsonDirectory: String, fileName: String): Long? {
    val details = getProgressiveJsonData(jsonDirectory, fileName)
    return details?.lastUpdated?.epochSeconds
}




