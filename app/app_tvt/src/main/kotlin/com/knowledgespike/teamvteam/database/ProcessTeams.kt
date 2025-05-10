package com.knowledgespike.teamvteam.database

import com.knowledgespike.extensions.generateTvTFileName
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.checkIfShouldProcess
import com.knowledgespike.shared.database.getCountOfMatchesBetweenTeams
import com.knowledgespike.shared.database.getCountryIdsFromName
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.json.getTvTJsonData
import org.jooq.SQLDialect


class ProcessTeams(
    allTeams: TeamNameToValidTeam,
    opponentsForTeam: Map<String, TeamNameToValidTeam>,
    private val opponentsWithAuthors: Map<String, List<Author>>,
) {
    val log by LoggerDelegate()

    private val idPairs: List<TeamsAndOpponents>

    init {
        idPairs = buildPairsOfTeamsThatMayCompete(allTeams, opponentsForTeam)
    }


    fun process(
        connection: java.sql.Connection,
        dialect: SQLDialect,
        countries: List<String>,
        matchSubType: String,
        jsonDirectory: String,
        competitionTeams: List<String>,
        overall: Boolean,
        callback: (teamPairDetails: TeamPairDetails, jsonDirectory: String) -> Unit,
    ): Map<String, TeamPairHomePagesData> {

        val matchType: String = matchTypeFromSubType(matchSubType)

        val countryIds = getCountryIdsFromName(countries, connection, dialect)

        var pairsForPage: Map<String, TeamPairHomePagesData> = mutableMapOf()



        for (teamsAndOpponents in idPairs) {
            val matchDto =
                getCountOfMatchesBetweenTeams(
                    connection,
                    dialect,
                    countryIds,
                    teamsAndOpponents,
                    matchSubType,
                    overall
                )
            if (matchDto.count + matchDto.abandoned + matchDto.cancelled != 0) {
                val teamPairDetails =
                    TeamPairDetails(
                        arrayOf(teamsAndOpponents.teamName, teamsAndOpponents.opponentsName),
                        matchDto
                    )

                val fileName = teamPairDetails.generateTvTFileName(matchSubType)

                val lastUpdatedDate = getLastUpdatedDate(jsonDirectory, fileName)

                pairsForPage = addPairToPage(
                    competitionTeams,
                    teamsAndOpponents.teamName,
                    teamsAndOpponents.opponentsName,
                    pairsForPage
                )


                if (lastUpdatedDate == null || checkIfShouldProcess(
                        connection,
                        dialect,
                        teamsAndOpponents.teamIds.map { it.teamId },
                        teamsAndOpponents.opponentIds.map { it.teamId },
                        matchType,
                        lastUpdatedDate
                    )
                ) {

                    val teamParams = getTeamParams(teamsAndOpponents, matchType, matchSubType)
                    log.info("About to process {}", teamParams)
                    teamPairDetails.addTeamData(
                        connection,
                        dialect,
                        countryIds,
                        teamParams.first,
                        teamParams.second
                    )
                    teamPairDetails.addIndividualData(
                        connection,
                        dialect,
                        countryIds,
                        teamParams.first,
                        teamParams.second,
                        matchType,
                        matchDto.matchIds
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

