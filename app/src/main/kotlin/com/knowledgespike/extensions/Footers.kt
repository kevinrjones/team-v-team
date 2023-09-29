package com.knowledgespike.extensions

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.br

fun DIV.generateRecordPageFooter(teamA: String, teamB: String, indexPageSuffix: String) {
    /*
    <p align="center">Up to <a href="auk_fc.html">Auckland index page</a> or <a href="can_fc.html">Canterbury index page</a><br>
    or
    <a href="nz_fc.html">New Zealand index page</a>
    or <a href="index.html">Country index page</a>
    or <a href="../index.html">Records and Statistics</a></p><!--#include virtual="/includes/footer.html" -->

     */
    p("", "align", "center") {
        +"Up to "
        a(href = "${teamA.replace(" ", "_")}_${indexPageSuffix}.html") {
            +"${teamA} index page"
        }
        +" or "
        a(href = "${teamB.replace(" ", "_")}_${indexPageSuffix}.html") {
            +"${teamB} index page"
        }
        br {}
        +" or "
        a(href = "index.html") {
            +"Team index page"
        }
        +" or "
        a(href = "../index.html") {
            +"Records and Statistics"
        }

    }
}

fun DIV.generateTeamVsTeamFooter() {
    /*
    <p align="center">Up to <a href="auk_fc.html">Auckland index page</a> or <a href="can_fc.html">Canterbury index page</a><br>
    or
    <a href="nz_fc.html">New Zealand index page</a>
    or <a href="index.html">Country index page</a>
    or <a href="../index.html">Records and Statistics</a></p><!--#include virtual="/includes/footer.html" -->

     */
    p("", "align", "center") {
        +"Up to "
        a(href = "index.html") {
            +"Team index page"
        }
        +" or "
        a(href = "../index.html") {
            +"Records and Statistics"
        }

    }
}

fun DIV.generateBetweenTeamsFooter() {
    p("", "align", "center") {
        +"Up to "
        a(href = "../index.html") {
            +"Records and Statistics"
        }

    }

}