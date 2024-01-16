package com.knowledgespike.shared.helpers

fun getWicket(wicket: Int): String = when (wicket) {
    1 -> "1st"
    2 -> "2nd"
    3 -> "3rd"
    else -> "${wicket}th"
}

fun getPlayerScores(name: String, score: Int, isNotOut: Boolean): String {
    if (name.lowercase() == "unknown") return "[unknown]"
    return if (isNotOut) "$name (${score}*)" else "$name (${score})"
}

fun getPartnership(partnership: Int, undefeated: Boolean): String =
    if (!undefeated) partnership.toString()
    else "${partnership}*"

fun getNotOutScore(score: Int, notOut: Boolean): String {
    return if (notOut) "$score*" else "$score"
}

fun isMatchTypeMultiInnings(matchType: String): Boolean {
    return (matchType == "f"
            || matchType == "t"
            || matchType == "wf"
            || matchType == "wt")
}
