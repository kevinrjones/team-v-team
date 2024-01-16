package com.knowledgespike.teamvteam.database

import com.knowledgespike.extensions.generateFileName
import com.knowledgespike.shared.TeamPairHomePagesData
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.shared.database.checkIfShouldProcess
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.Application.Companion.dialect
import com.knowledgespike.teamvteam.json.getTvTJsonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProcessTeams(
    allTeams: TeamNameToIds,
    opponentsForTeam: Map<String, TeamNameToIds>,
    private val opponentsWithAuthors: Map<String, List<Author>>,
) {
    val log by LoggerDelegate()

    private val idPairs: List<TeamsAndOpponents>

    init {
        idPairs = buildPairsOfTeamsThatMayCompete(allTeams, opponentsForTeam)
    }


    suspend fun process(
        connectionString: String,
        userName: String,
        password: String,
        matchSubType: String,
        jsonDirectory: String,
        competitionTeams: List<String>,
        callback: (teamPairDetails: TeamPairDetails, jsonDirectory: String) -> Unit
    ): Map<String, TeamPairHomePagesData> {

        val matchType: String = matchTypeFromSubType(matchSubType)

        var pairsForPage: Map<String, TeamPairHomePagesData> = mutableMapOf()

        for (teamsAndOpponents in idPairs) {
            log.debug("Start processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)

            val matchDto =
                getCountOfMatchesBetweenTeams(
                    connectionString,
                    userName,
                    password,
                    teamsAndOpponents,
                    matchSubType,
                    dialect
                )
            if (matchDto.count != 0) {
                log.debug("Processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)
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
                        connectionString,
                        userName,
                        password,
                        teamsAndOpponents.teamIds,
                        teamsAndOpponents.opponentIds,
                        matchType,
                        lastUpdatedDate,
                        dialect
                    )
                ) {


                    withContext(Dispatchers.IO) {
                        val job = launch {

                            val databaseConnection = DatabaseConnection(userName, password, connectionString)
                            val teamParams = getTeamParams(teamsAndOpponents, matchType, matchSubType)
                            teamPairDetails.addTeamData(databaseConnection, teamParams.first, teamParams.second)
                            teamPairDetails.addIndividualData(databaseConnection, teamParams.first, teamParams.second, matchType)

                            val maybeAuthors1 = opponentsWithAuthors
                                .filter { it.key == teamPairDetails.teams[0] }
                                .get(teamPairDetails.teams[0])?.map { it }
                                ?.filter { it.opponent == teamPairDetails.teams[1] }
                                ?.map { it.name }

                            val maybeAuthors2 = opponentsWithAuthors
                                .filter { it.key == teamPairDetails.teams[1] }
                                .get(teamPairDetails.teams[1])?.map { it }
                                ?.filter { it.opponent == teamPairDetails.teams[0] }
                                ?.map { it.name }

                            if (maybeAuthors1 != null && maybeAuthors1.isNotEmpty())
                                teamPairDetails.authors.addAll(maybeAuthors1)
                            if (maybeAuthors2 != null && maybeAuthors2.isNotEmpty())
                                teamPairDetails.authors.addAll(maybeAuthors2)

                        }

                        job.join()

                        callback(teamPairDetails, jsonDirectory)
                    }
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

