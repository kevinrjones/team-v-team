package com.knowledgespike.shared.html

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.br



fun DIV.generateBetweenTeamsFooter() {
    p("", "align", "center") {
        +"Up to "
        a(href = "../index.html") {
            +"Records and Statistics"
        }

    }

}