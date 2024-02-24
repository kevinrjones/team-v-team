package com.knowledgespike.shared.data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId

typealias TeamNameToIds = Map<String, List<Int>>

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
        "wid" -> "f"
        "o" -> "a"
        "a" -> "a"
        "wc" -> "a"

        "itt" -> "tt"
        "tt" -> "tt"
        "bbl" -> "tt"
        "ipl" -> "tt"
        "hund" -> "tt"
        "psl" -> "tt"
        "cpl" -> "tt"

        "wt" -> "wf"
        "wf" -> "wf"

        "wo" -> "wa"
        "wa" -> "wa"
        "wwc" -> "wa"

        "witt" -> "wtt"
        "wtt" -> "wtt"
        "wbbl" -> "wtt"
        "wipl" -> "wtt"
        "whund" -> "wtt"
        "wcpl" -> "wtt"

        "minc" -> "minc"
        else -> throw Exception("Unknown match sub type - please add the new subtype to type mapping")
    }
}
