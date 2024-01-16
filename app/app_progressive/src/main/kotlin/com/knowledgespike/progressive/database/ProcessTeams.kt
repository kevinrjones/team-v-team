package com.knowledgespike.progressive.database

import com.knowledgespike.progressive.json.getProgressiveJsonData
import com.knowledgespike.shared.TeamPairHomePagesData
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.shared.database.checkIfShouldProcess
import com.knowledgespike.shared.logging.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jooq.SQLDialect

class ProcessTeams(
    allTeams: TeamNameToIds,
    opponentsForTeam: Map<String, TeamNameToIds>,
    private val opponentsWithAuthors: Map<String, List<Author>>,
    private val dialect: SQLDialect
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
                        matchDto,
                        dialect
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
                            val teamParams = getTeamParams(teamsAndOpponents, matchType, matchSubType)
                            val databaseConnection = DatabaseConnection(userName, password, connectionString)
                            teamPairDetails.getFallOfWicketRecords(databaseConnection, teamParams.first, teamParams.second)
                            teamPairDetails.addTeamData(databaseConnection, teamParams.first, teamParams.second)
                            teamPairDetails.addIndividualData(databaseConnection, teamParams.first, teamParams.second)

                            val maybeAuthors1 = getAuthors(teamPairDetails, teamPairDetails.teams[0], teamPairDetails.teams[1])
                            val maybeAuthors2 = getAuthors(teamPairDetails, teamPairDetails.teams[1], teamPairDetails.teams[0])


                            if (!maybeAuthors1.isNullOrEmpty())
                                teamPairDetails.authors.addAll(maybeAuthors1)
                            if (!maybeAuthors2.isNullOrEmpty())
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


    private fun getAuthors(teamPairDetails: TeamPairDetails, author1: String, author2: String): List<String>? {
        val maybeAuthors1 = opponentsWithAuthors
            .filter { it.key == author1 }
            .get(teamPairDetails.teams[0])?.map { it }
            ?.filter { it.opponent == author2 }
            ?.map { it.name }
        return maybeAuthors1
    }
}


fun TeamPairDetails.generateFileName(
    matchSubType: String
): String {
    val fileName = "${this.teams[0].replace(" ", "_")}_v_${
        this.teams[1].replace(" ", "_")
    }_${matchSubType}.json"
    return fileName

}

fun getLastUpdatedDate(jsonDirectory: String, fileName: String): Long? {
    val details = getProgressiveJsonData(jsonDirectory, fileName)
    return details?.lastUpdated?.epochSeconds
}




