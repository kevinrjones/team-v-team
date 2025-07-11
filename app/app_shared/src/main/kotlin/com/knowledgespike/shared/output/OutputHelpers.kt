package com.knowledgespike.shared.output

import com.knowledgespike.shared.data.BestBowlingDto
import com.knowledgespike.shared.data.FoWDto
import com.knowledgespike.shared.data.HighestScoreDto
import com.knowledgespike.shared.data.NameUpdate
import kotlin.collections.forEach

fun updateSomeNamesToFullNameInFow(fow: FoWDto, nameUpdates: List<NameUpdate>): FoWDto {

    var newFow = fow

    nameUpdates.forEach { nameUpdate ->
        newFow =
            if (newFow.player1Name == nameUpdate.originalName
                && newFow.team == nameUpdate.team
                && newFow.opponents == nameUpdate.opponents
                && (newFow.seriesDate == nameUpdate.seriesDate || newFow.seriesDate == nameUpdate.matchDate)
                && newFow.location == nameUpdate.location
            ) {
                newFow.copy(player1Name = nameUpdate.replacementName)
            } else {
                newFow
            }

        newFow =
            if (newFow.player2Name == nameUpdate.originalName
                && newFow.team == nameUpdate.team
                && newFow.opponents == nameUpdate.opponents
                && (newFow.seriesDate == nameUpdate.seriesDate || newFow.seriesDate == nameUpdate.matchDate)
                && newFow.location == nameUpdate.location
            ) {
                newFow.copy(player2Name = nameUpdate.replacementName)
            } else {
                newFow
            }
    }

    return newFow
}


fun updateSomeNamesToFullNameInBestBowling(bestBowling: BestBowlingDto, nameUpdates: List<NameUpdate>): BestBowlingDto {
    var newBestBowling = bestBowling

    nameUpdates.forEach { nameUpdate ->
        newBestBowling =
            if (newBestBowling.name == nameUpdate.originalName
                && newBestBowling.team == nameUpdate.team
                && newBestBowling.opponents == nameUpdate.opponents
                && (newBestBowling.seriesDate == nameUpdate.seriesDate || newBestBowling.seriesDate == nameUpdate.matchDate)
                && newBestBowling.location == nameUpdate.location
            ) {
                newBestBowling.copy(name = nameUpdate.replacementName)
            } else {
                newBestBowling
            }
    }
    return newBestBowling
}

fun updateSomeNamesToFullNameInMostRuns(highestScore: HighestScoreDto, nameUpdates: List<NameUpdate>): HighestScoreDto {
    var newHs = highestScore

    nameUpdates.forEach { nameUpdate ->
        newHs =
            if (newHs.name == nameUpdate.originalName
                && newHs.team == nameUpdate.team
                && newHs.opponents == nameUpdate.opponents
                && (newHs.seriesDate == nameUpdate.seriesDate || newHs.seriesDate == nameUpdate.matchDate)
                && newHs.location == nameUpdate.location
            ) {
                newHs.copy(name = nameUpdate.replacementName)
            } else {
                newHs
            }
    }
    return newHs
}
