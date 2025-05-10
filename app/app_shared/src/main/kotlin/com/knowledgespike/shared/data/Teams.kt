package com.knowledgespike.shared.data

import com.knowledgespike.shared.types.TeamIdAndValidDate

fun buildPairsOfTeamsThatMayCompete(
    allTeams: TeamNameToValidTeam,
    opponentsForTeam: Map<String, TeamNameToValidTeam>
): List<TeamsAndOpponents> {

    val pairs = ArrayList<TeamsAndOpponents>()
    val teamNames = allTeams.keys.toTypedArray()

    val totalNumberOfTeams = teamNames.size
    for (i in 0 until totalNumberOfTeams) {
        val teamIdsAndValidDate = allTeams[teamNames[i]]!!
        for (j in i + 1 until totalNumberOfTeams) {
            val opponentIds = allTeams[teamNames[j]]!!
            pairs.add(
                TeamsAndOpponents(
                    teamName = teamNames[i],
                    teamIds = teamIdsAndValidDate,
                    opponentsName = teamNames[j],
                    opponentIds = opponentIds
                )
            )
        }
    }

    for (teamName in opponentsForTeam.keys) {
        val opponents = opponentsForTeam[teamName] ?: mapOf()
        val teamIds = allTeams[teamName] ?: listOf()

        opponents.keys.sorted().forEach { name ->
            val opponentIds = opponents[name] ?: listOf()
            pairs.add(
                TeamsAndOpponents(
                    teamName = teamName,
                    teamIds = teamIds,
                    opponentsName = name,
                    opponentIds = opponentIds
                )
            )
        }
    }
    return pairs
}

fun buildPairsOfTeamsOpponents(
    allTeams: TeamNameToValidTeam,
    opponentsForTeam: Map<String, TeamNameToValidTeam>
): Map<TeamAndIds, List<TeamIdAndValidDate>> {

    val teamNames = allTeams.keys.toTypedArray()

    val teamsToAllOpponents = mutableMapOf<TeamAndIds, MutableList<TeamIdAndValidDate>>()

    val totalNumberOfTeams = teamNames.size
    for (i in 0 until totalNumberOfTeams) {
        val teamName = teamNames[i]
        val teamIdsAndValidDate = allTeams[teamNames[i]]!!
        val teamAndIds = TeamAndIds(teamNames[i], teamIdsAndValidDate)

        val listOfIds = teamsToAllOpponents.getOrDefault(teamAndIds, mutableListOf())

        for (j in 0 until totalNumberOfTeams) {
            val opponentName = teamNames[j]
            if (opponentName != teamName) {
                val opponentIdsAndValidDate = allTeams[teamNames[j]]!!
                listOfIds.addAll(opponentIdsAndValidDate)
            }
        }
        teamsToAllOpponents.putIfAbsent(teamAndIds, listOfIds)
        val opponents = opponentsForTeam
            .getOrDefault(teamName, mapOf())
            .flatMap { it.value }

        teamsToAllOpponents[teamAndIds]?.addAll(opponents)
    }

    return teamsToAllOpponents
}

fun addPairToPage(
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

data class TeamParams(
    val teamIds: List<TeamIdAndValidDate>,
    val opponentIds: List<TeamIdAndValidDate>,
    val team: String,
    val opponents: String,
    val matchType: String,
    val matchSubType: String,
)

/**
 * Constructs two `TeamParams` objects, one representing the team and its opponents,
 * and the other representing the opponents and the team, with respective information
 * about team names, IDs, and match details.
 *
 * @param teamsAndOpponents an object containing the names and IDs of the team and its opponents
 *                          along with other match-related details.
 * @param matchType a string representing the type of the match.
 * @param matchSubType a string representing the subtype of the match.
 * @return a pair of `TeamParams` objects, where the first represents the team's perspective
 *         and the second the opponents' perspective.
 */
fun getTeamParams(
    teamsAndOpponents: TeamsAndOpponents,
    matchType: String,
    matchSubType: String
): Pair<TeamParams, TeamParams> {
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

    return Pair(teamParamA, teamParamB)
}
