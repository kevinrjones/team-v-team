package com.knowledgespike.shared.html

import com.knowledgespike.shared.data.TotalDto
import kotlinx.html.*

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
    matchType: String
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


fun TABLE.generateEmptyTotalRow(title: String, columnTwoWidth: String, columnFiveWidth: String) {
    tr {

        td {
            +title
        }
        td(null, "width", columnTwoWidth) {
        }
        td(null) {

        }
        td {

        }
        td(null, "width", columnFiveWidth) {
        }
    }
}

fun TABLE.generateTotalsRow(ndx: Int, total: TotalDto, columnTwoWidth: String, columnFiveWidth: String, block: TD.() -> Unit) {
    tr {
        td {
            if (ndx == 0) {
                block()
            } else {
                +""
            }
        }
        td(null, "width", columnTwoWidth) {
            if (total.wickets == 10)
                +"${total.total}"
            else if (total.declared)
                +"${total.total} for ${total.wickets} declared"
            else
                +"${total.total} for ${total.wickets}"
        }
        td {

        }
        td(null) {
            +total.location
        }
        td(null, "width", columnFiveWidth) {
            +total.seriesDate
        }
    }
}
