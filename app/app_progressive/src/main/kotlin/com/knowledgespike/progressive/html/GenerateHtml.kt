package com.knowledgespike.progressive.html


import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.progressive.json.ProgressiveData
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.helpers.*
import com.knowledgespike.shared.html.*
import com.knowledgespike.shared.logging.LoggerDelegate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.time.format.DateTimeFormatter

class GenerateHtml {

    private val columnOneWidth = "130"
    private val columnTwoWidth = "130"
    private val columnFiveWidth = "100"
    private val virtualHeader = "<!--#include virtual=\"/includes/header.html\" -->"
    private val virtualFooter = "<!--#include virtual=\"/includes/footer.html\" -->"

    private val log by LoggerDelegate()

    fun generateProgressiveRecordsPage(
        teamPairDetails: ProgressiveData,
        file: File
    ) {

        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        fileWriter.use {
            generateTeamVTeamHtml(teamPairDetails, fileWriter)
        }
        log.info("Completed: ${file.name}")

    }

    fun createProgressiveTeamPairHomePages(
        teamPairHomePages: TeamPairHomePagesJson,
        file: File
    ) {

        try {
            val teamName = teamPairHomePages.mainTeamName
            log.debug("createTeamPairHomePages for: {}", teamName)
            log.debug("createTeamPairHomePages fileName: {}", file.name)
            file.parentFile.mkdirs()

            val fileWriter = file.writer()

            fileWriter.use {
                fileWriter.append(virtualHeader)
                fileWriter.append("\r\n")
                // create entries for each pair
                fileWriter.appendHTML().div {
                    h3 {
                        +"${teamName}'s ${teamPairHomePages.matchDesignator} Records"
                    }
                    ul {
                        teamPairHomePages.teamNames.forEach { name ->
                            li {
                                log.debug(
                                    "createTeamPairHomePages, call  generateAnchorForTeamVsTeam for teamName: {}",
                                    teamName
                                )
                                generateAnchorForTeamVsTeam(
                                    teamName,
                                    name.first,
                                    name.second,
                                    teamPairHomePages.matchType
                                )
                            }
                        }
                    }
                    generateTeamVsTeamFooter()
                }
                fileWriter.append(virtualFooter)
                fileWriter.append("\r\n")
                log.info("Completed: ${file.name}")
            }
        } catch (e: Exception) {
            log.error("", e)
            throw e
        }
    }

    fun generateIndexPageForProgeressiveTeamsAndType(
        teamNames: List<String>,
        matchType: String,
        country: String,
        gender: String,
        matchDesignator: String,
        extraMessages: List<String>,
        fileName: String
    ) {


        val file = File(fileName)
        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        val capitalizedCountry: String = getCapitalizedCountryName(country)
        fileWriter.use {
            fileWriter.append(virtualHeader)
            fileWriter.append("\r\n")
            fileWriter.appendHTML().div {
                h3 {
                    +"$matchDesignator Records Between $gender Teams $capitalizedCountry"
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
            fileWriter.append(virtualFooter)
            fileWriter.append("\r\n")
            log.info("Completed: ${file.name}")
        }
    }

    private fun generateTeamVTeamHtml(
        teamPairDetails: ProgressiveData,
        outputStream: Appendable
    ) {

        outputStream.append(virtualHeader)
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
                            width: 100px;
                        }
                        
                        table.fowtable {
                            margin-top: 5px;
                        }
                        
                        table.numberOfMatchesTable, table.numberOfMatchesTable > tbody > tr, table.numberOfMatchesTable > tbody > tr > td {
                            border: none !important
                        }
                    """
                )
            }
        }

        outputStream.appendHTML().div {
            h3 {
                +"${teamPairDetails.team1} v ${teamPairDetails.team2} ${teamPairDetails.competitionTitle} Records"
            }
            generateHtml(teamPairDetails.competitionSubType, teamPairDetails)
        }

    }

    private fun DIV.generateHtml(matchType: String, teamPairDetails: ProgressiveData) {


        table(classes = "numberOfMatchesTable") {
            tr {
                td {
                    +"Number of Matches"
                }
                td(null, "width", columnTwoWidth) {
                    +"${teamPairDetails.matchDto.count}"
                }
                td {
                    +"From: "
                    +DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        .format(teamPairDetails.matchDto.startDate.toJavaLocalDateTime())
                }
                td {
                    +"to: "
                    +DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        .format(teamPairDetails.matchDto.endDate.toJavaLocalDateTime())
                }
                td(null, "width", columnFiveWidth) {

                }

            }
        }

        for (index in 0..1) {
            if (index == 0)
                h4 { +teamPairDetails.team1 }
            else
                h4 { +teamPairDetails.team2 }
            generateSingleMatchDataTable(matchType, teamPairDetails, index)
            generateFowHtml(teamPairDetails.bestFoW[index]) { wicket, teamA, teamB ->
                log.warn("MatchType: ${matchType}: FOW: wicket $wicket for $teamA vs $teamB has unknown players")
            }
//            generateOverallDataTable(teamPairDetails, index)
        }
        p { +teamPairDetails.authors.joinToString(", ") }
        generateRecordPageFooter(teamPairDetails.team1, teamPairDetails.team2, matchType)
    }

    private fun DIV.generateFowHtml(fows: Map<Int, FowDetails>, report: (Int, String, String) -> Unit) {
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
                                if (fow.player1Name.lowercase() == "unknown" && fow.player2Name.lowercase() == "unknown") {
                                    report(wicket, fow.team, fow.opponents)
                                    +" "
                                } else {
                                    if (fow.player1Position < fow.player2Position) {
                                        +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                        +" "
                                        +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                    } else {
                                        +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                        +" "
                                        +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                    }
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

                it.value.multiPlayerFow.forEach { multiPlayerFowDao: MultiPlayerFowDto ->
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
                                    report(wicket, fow.team, fow.opponents)
                                    +" "
                                } else {
                                    if (fow.player1Position < fow.player2Position) {
                                        +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                        +" "
                                        +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                    } else {
                                        +getPlayerScores(fow.player2Name, fow.player2Score, fow.player2NotOut)
                                        +" "
                                        +getPlayerScores(fow.player1Name, fow.player1Score, fow.player1NotOut)
                                    }
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

    private fun DIV.generateSingleMatchDataTable(
        matchType: String,
        teamPairDetails: ProgressiveData,
        index: Int
    ) {
        table {
            generateTeamScoreRow(teamPairDetails.highestScores[index], "Highest Team Total")
            generateTeamScoreRow(teamPairDetails.lowestAllOutScores[index], "Lowest All-out Team Total")
            generateMostRunsInInningsRow(teamPairDetails.highestIndividualScores[index])
            generateBestBowlingRow(teamPairDetails.bestBowlingInnings[index], "Best Bowling in innings")

            if (isMatchTypeMultiInnings(matchType)) {
                generateBestBowlingRow(teamPairDetails.bestBowlingMatch[index], "Best Bowling in match")
            }
        }
    }

    private fun TABLE.generateTeamScoreRow(totals: List<TotalDto>, title: String) {
        if (totals.isEmpty()) {
            generateEmptyTotalRow("Highest Team Total", columnTwoWidth, columnFiveWidth)
        } else {
            totals.forEachIndexed { ndx, total ->
                generateTotalsRow(ndx, total, columnTwoWidth, columnFiveWidth) {
                    +title
                }
            }
        }
    }

    private fun TABLE.generateMostRunsInInningsRow(highestScore: List<HighestScoreDto>) {
        if (highestScore.isEmpty()) {
            tr {
                td {
                    +"Most Runs in innings"
                }
                td(null, "width", columnTwoWidth) {
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
                    td(null, "width", columnTwoWidth) {
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

    private fun TABLE.generateBestBowlingRow(bestBowlingInnings: List<BestBowlingDto>, title: String) {
        if (bestBowlingInnings.isEmpty()) {
            tr {
                td {
                    +title
                }
                td(null, "width", columnTwoWidth) {
                }
                td {

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
                            +title
                    }
                    td(null, "width", columnTwoWidth) {
                        +"${formatOvers(bb.balls, bb.ballsPerOver)}-${bb.maidens}-${bb.runs}-${bb.wickets}"
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
                    td(null) {
                        +"(${bb.ballsPerOver} BpO)"
                    }
                }
            }
        }
    }

    private fun formatOvers(balls: Int, ballsPerOver: Int): String {
        val bpo = if(ballsPerOver == 0) 6 else ballsPerOver
        val overs = balls/bpo
        val ballsLeft = balls.mod(bpo)
        return if(ballsLeft == 0) {
            overs.toString()
        } else {
            "${overs}.$ballsLeft"
        }
    }
}


fun formatDouble(input: Double, scale: Int) = String.format("%.${scale}f", input)






