package com.knowledgespike.shared.data

fun buildPairsOfTeamsThatMayCompete(
    allTeams: TeamNameToIds,
    opponentsForTeam: Map<String, TeamNameToIds>
): List<TeamsAndOpponents> {

    val pairs = ArrayList<TeamsAndOpponents>()
    val teamNames = allTeams.keys.toTypedArray()

    val totalNumberOfTeams = teamNames.size
    for (i in 0 until totalNumberOfTeams) {
        val teamIds = allTeams[teamNames[i]]!!
        for (j in i + 1 until totalNumberOfTeams) {
            val opponentIds = allTeams[teamNames[j]]!!
            pairs.add(TeamsAndOpponents(teamNames[i], teamIds, teamNames[j], opponentIds))
        }
    }

    for (teamName in opponentsForTeam.keys) {
        val opponents = opponentsForTeam[teamName] ?: mapOf()
        val teamId = allTeams[teamName] ?: listOf()

        opponents.keys.sorted().forEach { name ->
            val opponentIds = opponents[name] ?: listOf()
            pairs.add(TeamsAndOpponents(teamName, teamId, name, opponentIds))
        }
    }
    return pairs
}

fun buildPairsOfTeamsOpponents(
    allTeams: TeamNameToIds,
    opponentsForTeam: Map<String, TeamNameToIds>
): Map<TeamAndIds, List<Int>> {

    val teamNames = allTeams.keys.toTypedArray()

    val teamsToAllOpponents = mutableMapOf<TeamAndIds, MutableList<Int>>()

    val totalNumberOfTeams = teamNames.size
    for (i in 0 until totalNumberOfTeams) {
        val teamName = teamNames[i]
        val teamIds = allTeams[teamNames[i]]!!
        val teamAndIds = TeamAndIds(teamNames[i], teamIds)

        val listOfIds = teamsToAllOpponents.getOrDefault(teamAndIds, mutableListOf())

        for (j in 0 until totalNumberOfTeams) {
            val opponentName = teamNames[j]
            if (opponentName != teamName) {
                val opponentIds = allTeams[teamNames[j]]!!
                listOfIds.addAll(opponentIds)
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
    val teamIds: List<Int>,
    val opponentIds: List<Int>,
    val team: String,
    val opponents: String,
    val matchType: String,
    val matchSubType: String,
)

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
