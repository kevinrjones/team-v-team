package com.knowledgespike.shared.data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId

val internationalMatchTypes = listOf("t", "wt", "itt", "witt", "o", "wo")

fun Long.toLocalDateTime(): kotlinx.datetime.LocalDateTime {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(this)
    val date = instant.toLocalDateTime(TimeZone.of(ZoneId.systemDefault().id))
    return date
}


fun matchTypeFromSubType(matchType: String): String {
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
