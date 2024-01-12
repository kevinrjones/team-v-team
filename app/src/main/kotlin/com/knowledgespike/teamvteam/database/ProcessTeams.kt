package com.knowledgespike.teamvteam.database

import com.knowledgespike.db.tables.references.MATCHES
import com.knowledgespike.db.tables.references.MATCHSUBTYPE
import com.knowledgespike.extensions.generateFileName
import com.knowledgespike.teamvteam.Application.Companion.dialect
import com.knowledgespike.teamvteam.TeamNameToIds
import com.knowledgespike.teamvteam.TeamPairHomePagesData
import com.knowledgespike.teamvteam.daos.MatchDto
import com.knowledgespike.teamvteam.data.Author
import com.knowledgespike.teamvteam.data.TeamsAndOpponents
import com.knowledgespike.teamvteam.json.getTvTJsonData
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import java.sql.DriverManager
import java.time.ZoneId


val internationalMatchTypes = listOf("t", "wt", "itt", "witt", "o", "wo")

class ProcessTeams(
    private val allTeams: TeamNameToIds,
    private val opponentsForTeam: Map<String, TeamNameToIds>,
    private val opponentsWithAuthors: Map<String, List<Author>>,
) {
    val log by LoggerDelegate()

    private val idPairs: List<TeamsAndOpponents>

    private fun buildPairsOfTeamsThatMayCompete(): List<TeamsAndOpponents> {

        val pairs = ArrayList<TeamsAndOpponents>()
        val teamNames = allTeams.keys.toTypedArray()

        val totalNumberOfTeams = teamNames.size
        for (i in 0 until totalNumberOfTeams) {
            val teamIds = allTeams.get(teamNames[i])!!
            for (j in i + 1 until totalNumberOfTeams) {
                val opponentIds = allTeams.get(teamNames[j])!!
                pairs.add(TeamsAndOpponents(teamNames[i], teamIds, teamNames[j], opponentIds))
            }
        }

        for (teamName in opponentsForTeam.keys) {
            val opponents = opponentsForTeam[teamName] ?: mapOf()
            val teamId = allTeams.get(teamName) ?: listOf()

            opponents.keys.sorted().forEach { name ->
                val opponentIds = opponents[name] ?: listOf()
                pairs.add(TeamsAndOpponents(teamName, teamId, name, opponentIds))
            }
        }
        return pairs
    }

    init {
        idPairs = buildPairsOfTeamsThatMayCompete()
    }

    private fun addPairToPageEx(
        competitionTeams: List<String>,
        team1: String,
        team2: String,
        pairsForPage: Map<String, TeamPairHomePagesData>
    ): Map<String, TeamPairHomePagesData> {

        val mutablePairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()
        mutablePairsForPage.putAll(pairsForPage)
        var teamName = team1
        var hasOwnPage = competitionTeams.contains(teamName)

        var pairEx = pairsForPage[teamName]

        if (pairEx == null) {
            val teamPairList = mutableListOf<Pair<String, String>>()
            val teamPair = Pair(team1, team2)
            teamPairList.add(teamPair)
            mutablePairsForPage[teamName] = TeamPairHomePagesData(hasOwnPage, teamPairList)
        } else {
            pairEx.teamPairDetails.add(Pair(team1, team2))
        }

        teamName = team2
        hasOwnPage = competitionTeams.contains(teamName)

        pairEx = pairsForPage[teamName]

        if (pairEx == null) {
            val teamPairList = mutableListOf<Pair<String, String>>()
            val teamPair = Pair(team1, team2)
            teamPairList.add(teamPair)
            mutablePairsForPage[teamName] = TeamPairHomePagesData(hasOwnPage, teamPairList)
        } else {
            pairEx.teamPairDetails.add(Pair(team1, team2))
        }

        return mutablePairsForPage
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

        val teamRecords = TeamRecords(userName, password, connectionString)
        for (teamsAndOpponents in idPairs) {
            log.debug("Start processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)

            val matchDto =
                getCountOfMatchesBetweenTeams(connectionString, userName, password, teamsAndOpponents, matchSubType)
            if (matchDto.count != 0) {
                log.debug("Processing: {} and {}", teamsAndOpponents.teamName, teamsAndOpponents.opponentsName)
                val teamPairDetails =
                    TeamPairDetails(
                        arrayOf(teamsAndOpponents.teamName, teamsAndOpponents.opponentsName),
                        matchDto
                    )

                val fileName = teamPairDetails.generateFileName(matchSubType)

                val lastUpdatedDate = getLastUpdatedDate(jsonDirectory, fileName)

                pairsForPage = addPairToPageEx(
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
                        lastUpdatedDate
                    )
                ) {


                    withContext(Dispatchers.IO) {
                        val job = launch {
                            getTeamRecords(teamPairDetails, teamRecords, teamsAndOpponents, matchType, matchSubType)
                            getIndividualRecords(
                                teamPairDetails,
                                teamRecords,
                                teamsAndOpponents,
                                matchType,
                                matchSubType
                            )

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

    private fun checkIfShouldProcess(
        connectionString: String,
        userName: String,
        password: String,
        teamIds: List<Int>,
        opponentIds: List<Int>,
        matchType: String,
        dateOffset: Long
    ): Boolean {
        DriverManager.getConnection(connectionString, userName, password).use { connection ->
            val context = DSL.using(connection, dialect)
            val res = context.select(MATCHES.ID).from(MATCHES).where(
                (MATCHES.HOMETEAMID.`in`(teamIds)
                    .or(MATCHES.HOMETEAMID.`in`(opponentIds)))
                    .and(
                        MATCHES.AWAYTEAMID.`in`(opponentIds)
                            .or(MATCHES.AWAYTEAMID.`in`(teamIds))
                    )
                    .and(MATCHES.MATCHTYPE.eq(matchType))
                    .and(MATCHES.ADDEDDATEASOFFSET.gt(dateOffset))
            ).fetch()

            if (res.isNotEmpty) return true
        }
        return false
    }


    private fun getCountOfMatchesBetweenTeams(
        connectionString: String,
        userName: String,
        password: String,
        teamsAndOpponents: TeamsAndOpponents,
        matchSubType: String
    ): MatchDto {

        val matchTypesToExclude = mutableListOf("t", "wt", "itt", "witt", "o", "wo")


        if (matchSubType == "minc")
            matchTypesToExclude.add("sec")

        DriverManager.getConnection(connectionString, userName, password).use { conn ->


            val context = DSL.using(conn, dialect)
            val result = context.select(
                count(),
                min(MATCHES.MATCHSTARTDATEASOFFSET).`as`("startDate"),
                max(MATCHES.MATCHSTARTDATEASOFFSET).`as`("endDate"),
            ).from(MATCHES).where(
                (MATCHES.HOMETEAMID.`in`(teamsAndOpponents.teamIds)
                    .or(MATCHES.HOMETEAMID.`in`(teamsAndOpponents.opponentIds)))
                    .and(
                        MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.opponentIds)
                            .or(MATCHES.AWAYTEAMID.`in`(teamsAndOpponents.teamIds))
                    )
                    .and(
                        MATCHES.ID.`in`(
                            select(MATCHSUBTYPE.MATCHID).from(
                                MATCHSUBTYPE.where(
                                    MATCHSUBTYPE.MATCHTYPE.eq(
                                        matchSubType
                                    )
                                )
                            )
                        )
                    )
                    .and(MATCHES.VICTORYTYPE.notEqual(6))
                    .and(MATCHES.VICTORYTYPE.notEqual(11))
                    .and(MATCHES.MATCHTYPE.notIn(matchTypesToExclude))
            ).fetch().first()

            val startDate: LocalDateTime = (result.getValue("startDate", Long::class.java) * 1000).toLocalDateTime()
            val endDate = (result.getValue("endDate", Long::class.java) * 1000).toLocalDateTime()
            return MatchDto(
                result.getValue(0, Int::class.java),
                startDate,
                endDate,
            )
        }
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        val instant = Instant.fromEpochMilliseconds(this)
        val date = instant.toLocalDateTime(TimeZone.of(ZoneId.systemDefault().id))
        return date
    }

    private fun getIndividualRecords(
        teamPairDetails: TeamPairDetails,
        teamRecords: TeamRecords,
        teamsAndOpponents: TeamsAndOpponents,
        matchType: String,
        matchSubType: String,
    ) {

        val teamParamA = TeamParams(
            teamsAndOpponents.teamIds,
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamName,
            teamsAndOpponents.opponentsName,
            matchType,
            matchSubType
        )
        val teamParamB = TeamParams(
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamIds,
            teamsAndOpponents.opponentsName,
            teamsAndOpponents.teamName,
            matchType,
            matchSubType
        )

        teamPairDetails.addIndividualData(teamRecords, teamParamA, teamParamB, matchType)

    }

    private fun getTeamRecords(
        teamPairDetails: TeamPairDetails,
        tt: TeamRecords,
        teamsAndOpponents: TeamsAndOpponents,
        matchType: String,
        matchSubType: String
    ) {
        val teamParamA = TeamParams(
            teamsAndOpponents.teamIds,
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamName,
            teamsAndOpponents.opponentsName,
            matchType,
            matchSubType
        )
        val teamParamB = TeamParams(
            teamsAndOpponents.opponentIds,
            teamsAndOpponents.teamIds,
            teamsAndOpponents.opponentsName,
            teamsAndOpponents.teamName,
            matchType,
            matchSubType
        )

        teamPairDetails.addTeamData(tt, teamParamA, teamParamB)

    }
}

private fun matchTypeFromSubType(matchType: String): String {
    return when (matchType) {
        "t" -> "f"
        "f" -> "f"
        "o" -> "a"
        "a" -> "a"
        "itt" -> "tt"
        "tt" -> "tt"
        "bbl" -> "tt"
        "ipl" -> "tt"
        "hund" -> "tt"

        "wt" -> "wf"
        "wf" -> "wf"
        "wo" -> "wa"
        "wa" -> "wa"
        "witt" -> "wtt"
        "wtt" -> "wtt"
        "wbbl" -> "wtt"
        "wipl" -> "wtt"
        "whund" -> "wtt"
        "cpl" -> "tt"
        "wcpl" -> "wtt"
        "minc" -> "minc"
        "wc" -> "a"
        "wwc" -> "wa"
        "psl" -> "tt"
        else -> throw Exception("Unknown match sub type - please add the new subtype to type mapping")
    }
}