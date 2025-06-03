package com.knowledgespike.shared.output

import com.knowledgespike.shared.data.FoWDto
import com.knowledgespike.shared.data.NameUpdate
import kotlin.collections.forEach

fun updateSomeNameToFullName(fow: FoWDto, nameUpdates: List<NameUpdate>): FoWDto {

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
