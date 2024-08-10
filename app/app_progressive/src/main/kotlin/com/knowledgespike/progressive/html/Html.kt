package com.knowledgespike.progressive.html

import com.knowledgespike.shared.data.TotalDto
import com.knowledgespike.shared.html.td
import kotlinx.html.TABLE
import kotlinx.html.TD
import kotlinx.html.tr

fun TABLE.generateTotalsRow(
    ndx: Int,
    total: TotalDto,
    isFirstTeam: Boolean = true,
    generateRecordsForAllOpponents: Boolean = false,
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
        td {
            if (total.wickets == 10)
                +"${total.total}"
            else if (total.declared)
                +"${total.total} for ${total.wickets} declared"
            else
                +"${total.total} for ${total.wickets}"
        }
        td {
            if(generateRecordsForAllOpponents) {
                if (isFirstTeam) {
                    +"v ${total.opponents}"
                } else {
                    +total.opponents
                }
            }
        }
        td(null) {
            +total.location
        }
        td {
            +total.seriesDate
        }
    }
}

fun TABLE.generateEmptyTotalRow(title: String) {
    tr {

        td {
            +title
        }
        td {
        }
        td(null) {

        }
        td {

        }
        td {
        }
    }
}


