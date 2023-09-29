package com.knowledgespike.teamvteam.data

import kotlinx.serialization.Serializable

/*
[
    "competition" : {
        "title" : "Cambridge University vs Oxford University"
        "teams" :["a", "b"],
        "duplicates" : [
            "teamNames": ["Kings XI Punjab", "Punjab Kings"]
        ]
        "type": "bbl",
        "extraMessage" : "Performances involving Punjab Kings include those involving Kings XI Punjab between 2008 and 2020"
    },
   "competition" : {
        "title" : "Cambridge University vs Oxford University"
        "teams" :["a", "b"],
        "duplicates" : [
            "teamNames": ["Kings XI Punjab", "Punjab Kings"]
        ]
        "type": "bbl",
        "extraMessage" : "Performances involving Punjab Kings include those involving Kings XI Punjab between 2008 and 2020'"
    },
]
 */

@Serializable
data class Competition(
    val title: String, val gender: String, val country: String, val outputDirectory: String,
    val teams: List<Team>, val type: String, val extraMessages: List<String>
)

@Serializable
data class Team(val team: String, val duplicates: List<String>)

data class TeamsAndOpponents(val teamName: String, val teamdIds: List<Int>, val opponentsName: String, val opponentIds: List<Int>)