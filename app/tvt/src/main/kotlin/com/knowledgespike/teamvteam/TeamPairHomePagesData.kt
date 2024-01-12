package com.knowledgespike.teamvteam

import com.knowledgespike.teamvteam.json.TeamPairDetailsData

/**
 * If teams have 'opponents' in the JSON then those opponents don't have a top level HTML page as it is never linked
 * to from any other page. Only the A v B teams have top level pages (see ou_v_cu.json for an example)
 */
data class TeamPairHomePagesData(
    val shouldHaveOwnPage: Boolean,
    val teamPairDetails: MutableList<Pair<String, String>>
)