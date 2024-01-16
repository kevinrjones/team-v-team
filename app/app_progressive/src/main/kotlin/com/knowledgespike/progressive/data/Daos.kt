package com.knowledgespike.progressive.data

import kotlinx.serialization.Serializable

@Serializable
data class BestBowlingDto(
    val name: String,
    val team: String,
    val opponents: String,
    val ballsPerOver: Int,
    val balls: Int,
    val maidens: Int,
    val wickets: Int,
    val runs: Int,
    val location: String,
    val seriesDate: String,
)
