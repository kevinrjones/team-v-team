package com.knowledgespike.teamvteam.helpers

fun getWicket(wicket: Int): String = when (wicket) {
    1 -> "1st"
    2 -> "2nd"
    3 -> "3rd"
    else -> "${wicket}th"
}