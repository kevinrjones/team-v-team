package com.knowledgespike.teamvteam.html


import com.knowledgespike.extensions.capitalize

import com.knowledgespike.teamvteam.database.TeamPairDetails
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import com.knowledgespike.extensions.*
import com.knowledgespike.teamvteam.TeamPairHomePagesData
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.teamvteam.helpers.getWicket
import java.io.File

class GenerateHtml {

    val header = "<!--#include virtual=\"/includes/header.html\" -->"
    val footer = "<!--#include virtual=\"/includes/footer.html\" -->"

    val log by LoggerDelegate()

    fun generateTeamVsTeamRecordsPage(
        teamPairDetails: TeamPairDetails,
        matchDesignator: String,
        matchType: String,
        country: String,
        outputDirectory: String
    ) {

        val fileName =
            "${outputDirectory}/${
                teamPairDetails.teamA.replace(
                    " ",
                    "_"
                )
            }_v_${teamPairDetails.teamB.replace(" ", "_")}_${matchType}.html"

        val file = File(fileName)
        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        fileWriter.use {
            generateTeamVTeamHtml(teamPairDetails, matchDesignator, matchType, country, fileWriter)
        }
        log.info("Completed: $fileName")

    }

    fun createTeamPairHomePages(
        matchType: String,
        matchDesignator: String,
        pairsForPage: MutableMap<String, TeamPairHomePagesData>,
        country: String,
        gender: String,
        outputDirectory: String

    ) {
        // for each entry in the pairsForPage collection generate the HTML for the page
        // see: http://archive.acscricket.com/records_and_stats/team_v_team_fc/can_fc.html
        pairsForPage.forEach { (teamName, teamPairHomePagesData) ->
            try {
                if (teamPairHomePagesData.shouldHaveOwnPage) {
                    log.debug("createTeamPairHomePages for: {}", teamName)
                    val fileName =
                        "${outputDirectory}/${teamName.replace(" ", "_")}_${matchType}.html"
                    val file = File(fileName)
                    log.debug("createTeamPairHomePages fileName: {}", fileName)
                    file.parentFile.mkdirs()

                    val fileWriter = file.writer()

                    fileWriter.use {
                        fileWriter.append(header)
                        fileWriter.append("\r\n")
                        // create entries for each pair
                        fileWriter.appendHTML().div {
                            h3 {
                                +"${teamName}'s ${matchDesignator} Records"
                            }
                            ul {
                                teamPairHomePagesData.teamPairDetails.forEach { teamPairDetails ->
                                    li {
                                        log.debug(
                                            "createTeamPairHomePages, call  generateAnchorForTeamVsTeam for teamName: {}",
                                            teamName
                                        )
                                        generateAnchorForTeamVsTeam(teamName, teamPairDetails, matchType)
                                    }
                                }
                            }
                            log.debug(
                                "createTeamPairHomePages, call  generateTeamVsTeamFooter for gender: {}, country: {} and matchType: {}",
                                gender,
                                country,
                                matchType
                            )
                            generateTeamVsTeamFooter()
                        }
                        fileWriter.append(footer)
                        fileWriter.append("\r\n")
                    }
                }
            } catch (e: Exception) {
                log.error("", e)
                throw e
            }
        }
    }

    fun generateIndexPageForTeamsAndType(
        teamNames: List<String>,
        matchType: String,
        country: String,
        gender: String,
        matchDesignator: String,
        outputDirectory: String,
        extraMessages: List<String>
    ) {

        val fileName = "${outputDirectory}/index.html"


        val file = File(fileName)
        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        val capitalizedCountry: String = getCapitalizedCountryName(country)
        fileWriter.use {
            fileWriter.append(header)
            fileWriter.append("\r\n")
            fileWriter.appendHTML().div {
                h3 {
                    +"${matchDesignator} Records Between ${gender} Teams ${capitalizedCountry}"
                }
                ul {
                    teamNames.forEach {
                        this.li {
                            a(href = "${it.replace(" ", "_")}_${matchType}.html") {
                                +it
                            }
                        }
                    }
                }
                extraMessages.forEach { message ->
                    p {
                        i {
                            +message
                        }
                    }
                }
                generateBetweenTeamsFooter()
            }
            fileWriter.append(footer)
            fileWriter.append("\r\n")

        }
    }

    private fun getCapitalizedCountryName(country: String): String {
        return when (val caitalizedCountry = country.capitalize()) {
            "England" -> "in $caitalizedCountry"
            "Australia" -> "in $caitalizedCountry"
            "South Africa" -> "in $caitalizedCountry"
            "New Zealand" -> "in $caitalizedCountry"
            "West Indies" -> "in $caitalizedCountry"
            "India" -> "in $caitalizedCountry"
            "Pakistan" -> "in $caitalizedCountry"
            "Bangladesh" -> "in $caitalizedCountry"
            "Sri Lanka" -> "in $caitalizedCountry"
            "Afghanistan" -> "in $caitalizedCountry"
            "Ireland" -> "in $caitalizedCountry"
            else -> ""
        }
    }

    // Appendable is any of
    // BufferedWriter, CharArrayWriter, CharBuffer, FileWriter, FilterWriter, LogStream, OutputStreamWriter,
    // PipedWriter, PrintStream, PrintWriter, StringBuffer, StringBuilder, StringWriter, Writer
    private fun generateTeamVTeamHtml(
        teamPairDetails: TeamPairDetails,
        matchDesignator: String,
        matchType: String,
        country: String,
        outputStream: Appendable
    ) {

        outputStream.append(header)
        outputStream.append("\r\n")

        outputStream.appendHTML().style {
            unsafe {
                raw(
                    """                      
                        .fowtable > tbody > tr > td:nth-child(1) {
                            width: 100px;
                        }
                        .fowtable > tbody > tr > td:nth-child(2) {
                            width: 50px;
                        }
                        .fowtable > tbody > tr > td:nth-child(3) {
                        }
                        .fowtable > tbody > tr > td:nth-child(4) {
                            width: 300px;
                        }
                        .fowtable > tbody > tr > td:nth-child(5) {
                            width: 80px;
                        }
                        
                        table.fowtable {
                            margin-top: 5px;
                        }
                    """
                )
            }
        }

        outputStream.appendHTML().div {
            h3 {
                +"${teamPairDetails.teamA} v ${teamPairDetails.teamB} ${matchDesignator} Records"
            }
            generateScoresHtml(teamPairDetails, matchType, country)
        }

        outputStream.append(footer)
        outputStream.append("\r\n")
    }

    private fun LI.generateAnchorForTeamVsTeam(
        teamName: String,
        teamPairDetails: TeamPairDetails,
        matchType: String
    ) {

        val text = if (teamName == teamPairDetails.teamA) {
            "${teamPairDetails.teamA} v ${teamPairDetails.teamB} "
        } else {
            "${teamPairDetails.teamB} v ${teamPairDetails.teamA} "
        }

        a(
            href = "${teamPairDetails.teamA.replace(" ", "_")}_v_${
                teamPairDetails.teamB.replace(
                    " ",
                    "_"
                )
            }_${matchType}.html"
        ) {
            +text
        }
    }


    private fun DIV.generateScoresHtml(teamPairDetails: TeamPairDetails, matchType: String, country: String) {
        h4 { +teamPairDetails.teamA }
        table {
            generateHighestScoreRow(teamPairDetails.teamAHighestScores)
            generateLowestScoreRow(teamPairDetails.teamALowestScores)
            generateMostRunsInInningsRow(teamPairDetails.teamAHighestIndividualScore)
            generateBestBowlingInInningsRow(teamPairDetails.teamABestBowlingInnings)
            if (isMatchTypeMultiInnings(matchType)) {
                generateBestBowlingInMatchRow(teamPairDetails.teamABestBowlingMatch)
            }
        }
        generateFowHtml(teamPairDetails.teamABestFoW) { wicket ->
            log.warn("MatchType: ${matchType}: FOW: wicket $wicket for ${teamPairDetails.teamA} vs ${teamPairDetails.teamB} has unknown players")
        }
        h4 { +teamPairDetails.teamB }
        table {
            generateHighestScoreRow(teamPairDetails.teamBHighestScores)
            generateLowestScoreRow(teamPairDetails.teamBLowestScores)
            generateMostRunsInInningsRow(teamPairDetails.teamBHighestIndividualScore)
            generateBestBowlingInInningsRow(teamPairDetails.teamBBestBowlingInnings)
            if (isMatchTypeMultiInnings(matchType)) {
                generateBestBowlingInMatchRow(teamPairDetails.teamBBestBowlingMatch)
            }
        }
        generateFowHtml(teamPairDetails.teamBBestFoW) { wicket ->
            log.warn("MatchType: ${matchType}: FOW: wicket $wicket for ${teamPairDetails.teamB} vs ${teamPairDetails.teamA} has unknown players")
        }
        p { +"Kevin Jones" }
        generateRecordPageFooter(teamPairDetails.teamA, teamPairDetails.teamB, matchType)
    }

    private fun isMatchTypeMultiInnings(matchType: String): Boolean {
        return (matchType == "f"
                || matchType == "t"
                || matchType == "wf"
                || matchType == "wt")
    }


    private fun TABLE.generateHighestScoreRow(totals: MutableList<TotalDao>) {
        if (totals.size == 0) {
            tr {
                td {
                    +"Highest Team Total"
                }
                td(null, "width", "110") {
                    +"(none)"
                }
                td {

                }
                td {
                    +"(none)"
                }
                td {
                    +"(none)"
                }
            }
        } else {
            totals.forEachIndexed { ndx, total ->

                tr {
                    td {
                        if (ndx == 0)
                            +"Highest Team Total"
                        else {
                            +""
                        }
                    }

                    td(null, "width", "110") {
                        +"${total.total}"
                    }
                    td {

                    }
                    td(null) {
                        +total.location
                    }
                    td(null, "width", "80") {
                        +total.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateBestBowlingInMatchRow(bestBowlingMatch: MutableList<BestBowlingDao>) {
        if (bestBowlingMatch.size == 0) {
            tr {
                td {
                    +"Best Bowling in match"
                }
                td(null, "width", "100") {

                }
                td {

                }
                td {

                }
                td {

                }
            }
        } else {
            bestBowlingMatch.forEachIndexed { ndx, bb ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Best Bowling in match"
                    }
                    td(null, "width", "110") {
                        +"${bb.wickets}-${bb.runs}"
                    }
                    td {
                        +bb.name
                    }
                    td(null) {
                        +bb.location
                    }
                    td(null) {
                        +bb.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateBestBowlingInInningsRow(bestBowlingInnings: MutableList<BestBowlingDao>) {
        if (bestBowlingInnings.size == 0) {
            tr {
                td {
                    +"Best Bowling in innings"
                }
                td(null, "width", "110") {
                }
                td {

                }
                td {
                }
                td {
                }
            }
        } else {
            bestBowlingInnings.forEachIndexed { ndx, bb ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Best Bowling in innings"
                    }
                    td(null, "width", "110") {
                        +"${bb.wickets}-${bb.runs}"
                    }
                    td {
                        +bb.name
                    }

                    td(null) {
                        +bb.location
                    }
                    td(null) {
                        +bb.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateMostRunsInInningsRow(highestScore: MutableList<HighestScoreDao>) {
        if (highestScore.size == 0) {
            tr {
                td {
                    +"Most Runs in innings"
                }
                td(null, "width", "110") {
                }
                td {

                }
                td {
                }
                td {
                }
            }
        } else {
            highestScore.forEachIndexed { ndx, score ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Most Runs in innings"
                    }
                    td(null, "width", "110") {
                        +getNotOutScore(score.score, score.notOut)
                    }
                    td {
                        +score.name
                    }
                    td(null) {
                        +score.location
                    }
                    td(null) {
                        +score.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateLowestScoreRow(totals: MutableList<TotalDao>) {
        if (totals.size == 0) {
            tr {

                td {
                    +"Lowest All-Out Team Total"
                }
                td(null, "width", "110") {
                }
                td(null) {

                }
                td {

                }
                td(null) {
                }
            }
        } else {
            totals.forEachIndexed { ndx, total ->
                tr {
                    td {
                        if (ndx == 0) {
                            +"Lowest All-Out Team Total"
                        } else {
                            +""
                        }
                    }
                    td(null, "width", "110") {
                        +"${total.total}"
                    }
                    td {

                    }
                    td(null) {
                        +total.location
                    }
                    td(null) {
                        +total.seriesDate
                    }

                }
            }
        }
    }

    private fun DIV.generateFowHtml(fows: MutableMap<Int, FowDetails>, report: (Int) -> Unit) {
        table(classes = "fowtable") {

            fows.forEach {
                if (it.value.standardFow.isEmpty()) {
                    tr {
                        td {

                        }
                        td {

                        }
                        td {

                        }
                        td(null) {

                        }
                        td(null) {

                        }
                    }
                } else {

                    it.value.standardFow.forEachIndexed { ndx, fow ->
                        tr {
                            val wicket = it.key

                            td(null) {
                                if (ndx == 0)
                                    +"${getWicket(wicket)} Wkt"
                            }
                            td(null) {
                                +getPartnership(fow.partnership, fow.undefeated)
                            }
                            td {
                                if (fow.player1Name.lowercase() == "unknown" || fow.player2Name.lowercase() == "unknown") {
                                    report(wicket)
                                    +" "
                                } else {
                                    +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                    +" "
                                    +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                }
                            }
                            td(null) {
                                +fow.location
                            }
                            td(null) {
                                +fow.seriesDate
                            }
                        }
                    }
                }
            }
            fows.forEach {

                it.value.multiPlayerFow.forEachIndexed { ndx, multiPlayerFowDao: MultiPlayerFowDao ->
                    val fowList = multiPlayerFowDao.playerDetails
                    tr {
                        td {
                            +"Note:"
                        }
                        td(null, "colspan", "4") {// colspan=4
                            +"A total of ${multiPlayerFowDao.total} was added for the ${getWicket(multiPlayerFowDao.wicket)} wicket"
                        }
                    }
                    fowList.forEach { fow ->
                        tr {
                            val wicket = multiPlayerFowDao.wicket

                            td(null) {
                            }
                            td(null) {
                                +getPartnership(fow.partnership, fow.undefeated)
                            }
                            td {
                                if (fow.player1Name.lowercase() == "unknown" || fow.player2Name.lowercase() == "unknown") {
                                    report(wicket)
                                    +" "
                                } else {
                                    +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                    +" "
                                    +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                }
                            }
                            td(null) {
                                +fow.location
                            }
                            td(null) {
                                +fow.seriesDate
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getPlayerScores(name: String, score: Int, isNotOut: Boolean): String =
        if (isNotOut) "$name (${score}*)" else "$name (${score})"

    private fun getPartnership(partnership: Int, undefeated: Boolean): String =
        if (!undefeated) partnership.toString()
        else "${partnership}*"

    private fun getNotOutScore(score: Int, notOut: Boolean): String {
        return if (notOut) "$score*" else "$score"
    }
}





