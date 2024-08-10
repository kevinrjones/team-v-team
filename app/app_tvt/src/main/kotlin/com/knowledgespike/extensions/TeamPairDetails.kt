package com.knowledgespike.extensions

import com.knowledgespike.teamvteam.database.TeamPairDetails

fun TeamPairDetails.generateTvTFileName(
    matchSubType: String
): String {
    val fileName = "${this.teams[0].replace(" ", "_")}_v_${
        this.teams[1].replace(" ", "_")
    }_${matchSubType}.json"
    return fileName
}
