package com.knowledgespike.shared.types

data class TeamIdsAndValidDate(val teamIds: List<Int>, val startFrom: Long = -9999999999)
data class TeamIdAndValidDate(val teamId: Int, val startFrom: Long = -9999999999)