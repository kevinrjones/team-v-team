package com.knowledgespike.shared.html

import com.knowledgespike.shared.data.TotalDto
import kotlinx.html.*
import java.awt.print.Book

inline fun DIV.p(classes: String? = null, vararg attributes: String?, crossinline block: P.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    P(classAttr, consumer).visit(block)
}


inline fun TR.td(classes: String? = null, vararg attributes: String?, crossinline block: TD.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    TD(classAttr, consumer).visit(block)
}

inline fun TR.th(classes: String? = null, vararg attributes: String?, crossinline block: TD.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    TD(classAttr, consumer).visit(block)
}

fun LI.generateAnchorForTeamVsTeam(
    teamName: String,
    team1: String,
    team2: String,
    matchType: String,
) {

    val text = if (teamName == team1) {
        "${teamName} v ${team2} "
    } else {
        "${teamName} v ${team1} "
    }

    a(
        href = "${team1.replace(" ", "_")}_v_${
            team2.replace(
                " ",
                "_"
            )
        }_${matchType}.html"
    ) {
        +text
    }
}

fun generateHeaderPart(teamName: String, gender: String, matchDesignator: String): String {
    var name = if (gender.isEmpty()) {
        "$teamName's"
    } else {
        "$teamName $gender"
    }

    name = name.replace("Women Women", "Women")

    return "$name ${matchDesignator}"
}


