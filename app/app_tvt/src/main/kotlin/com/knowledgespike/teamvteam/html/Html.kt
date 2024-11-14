package com.knowledgespike.teamvteam.html

import com.knowledgespike.shared.data.TotalDto
import com.knowledgespike.shared.html.td
import kotlinx.html.TABLE
import kotlinx.html.TD
import kotlinx.html.tr

fun TABLE.generateTotalsRow(
    ndx: Int,
    total: TotalDto,
    columnTwoWidth: String,
    columnDateWidth: String,
    isFirstTeam: Boolean = false,
    block: TD.() -> Unit,
) {
    tr {
        td {
            if (ndx == 0) {
                block()
            } else {
                +""
            }
        }
        td(null, "width", columnTwoWidth) {
            if (total.allOut)
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
        td(null, "width", columnDateWidth) {
            +total.seriesDate
        }
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


