package com.knowledgespike.progressive.html


import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.progressive.json.ProgressiveData
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.html.*
import com.knowledgespike.shared.logging.LoggerDelegate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.time.format.DateTimeFormatter

class GenerateHtml {

    private val virtualHeader = "<!--#include virtual=\"/includes/header.html\" -->"
    private val virtualFooter = "<!--#include virtual=\"/includes/footer.html\" -->"

    private val log by LoggerDelegate()

    fun generateProgressiveRecordsPage(
        teamPairDetails: ProgressiveData,
        file: File,
        generateRecordsForAllOpponents: Boolean = false,
    ) {

        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        fileWriter.use {
            generateTeamVTeamHtml(teamPairDetails, fileWriter, generateRecordsForAllOpponents)
        }
        log.info("Completed: ${file.name}")

    }

    fun createProgressiveTeamPairHomePages(
        teamPairHomePages: TeamPairHomePagesJson,
        file: File,
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
                        val headerPart = generateHeaderPart(teamName, teamPairHomePages.gender, teamPairHomePages.matchDesignator)
                        +"${headerPart} Progressive Records"
                    }
                    ul {
                        li {
                            a(
                                href = "${teamName.replace(" ", "_")}_v_all_${teamPairHomePages.matchType}.html"
                            ) {
                                +"${teamName} v All Teams"
                            }
                        }
                        teamPairHomePages.teamNames.forEach { name ->
                            li {
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
        gender: String,
        country: String?,
        matchDesignator: String,
        extraMessages: List<String>,
        fileName: String,
    ) {


        val file = File(fileName)
        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        fileWriter.use {
            fileWriter.append(virtualHeader)
            fileWriter.append("\r\n")
            fileWriter.appendHTML().div {
                h3 {
                    +buildHeader(matchDesignator, gender, country)
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

    private fun DIV.generateBetweenTeamsFooter() {
        p("", "align", "center") {
            +"Up to "
            a(href = "../index.html") { +"Countries" }
            +" or "
            a(href = "../../index.html") {
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
            a(href = "../../index.html") {
                +"Records and Statistics"
            }

        }
    }

    private fun buildHeader(matchDesignator: String, gender: String, country: String?): String {
        return if (country != null) {
            "$matchDesignator Progressive Records Between $country $gender Teams"
        } else {
            "$matchDesignator Progressive Records Between $gender Teams"
        }
    }

    private fun generateTeamVTeamHtml(
        teamPairDetails: ProgressiveData,
        outputStream: Appendable,
        generateRecordsForAllOpponents: Boolean = false,
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
                            width: 300px;
                        }
                        .fowtable > tbody > tr > td:nth-child(4) {                            
                            width: 200px;
                        }
                        .fowtable > tbody > tr > td:nth-child(5) {
                            width: 300px;
                        }
                        .fowtable > tbody > tr > td:nth-child(6) {
                            width: 100px;
                        }
                        
                        table.fowtable {
                            margin-top: 5px;
                        }
                        
                        table.numberOfMatchesTable, table.numberOfMatchesTable > tbody > tr, table.numberOfMatchesTable > tbody > tr > td {
                            border: none !important
                        }
                        
                        table.teamTotalsTable > tbody > tr > td:nth-child(1) {
                            width: 100px;
                        }
                        table.teamTotalsTable > tbody > tr > td:nth-child(2) {
                            width: 150px;
                        }
                        table.teamTotalsTable > tbody > tr > td:nth-child(3) {
                            width: 150px;
                        }
                        table.teamTotalsTable > tbody > tr > td:nth-child(4) {
                            width: 300px;
                        }
                        table.teamTotalsTable > tbody > tr > td:nth-child(5) {
                            width: 150px;
                        }

                        table.teamScoresTable > tbody > tr > td:nth-child(1) {
                            width: 100px;
                        }
                        table.teamScoresTable > tbody > tr > td:nth-child(2) {
                            width: 80px;
                        }
                        table.teamScoresTable > tbody > tr > td:nth-child(3) {
                            width: 150px;
                        }
                        table.teamScoresTable > tbody > tr > td:nth-child(4) {
                            width: 300px;
                        }
                        table.teamScoresTable > tbody > tr > td:nth-child(5) {
                            width: 150px;
                        }
                        
                        
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(1) {
                            width: 100px;
                        }
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(2) {
                            width: 50px;
                        }
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(3) {
                            width: 150px;
                        }
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(4) {
                            width: 150px;
                        }
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(5) {
                            width: 300px;
                        }
                        table.mostRunsInningsTable > tbody > tr > td:nth-child(6) {
                            width: 150px;
                        }
                        
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(1) {
                            width: 120px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(2) {
                            width: 120px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(3) {
                            width: 150px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(4) {
                            width: 150px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(5) {
                            width: 300px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(6) {
                            width: 100px;
                        }
                        table.bestBowlingRowsTable > tbody > tr > td:nth-child(7) {
                            width: 100px;
                        }
                                               
                        
                        
                    """
                )
            }
        }

        outputStream.appendHTML().div {
            h3 {
                val name = if (generateRecordsForAllOpponents) {
                    "${teamPairDetails.team2} Teams"
                } else {
                    teamPairDetails.team2
                }
                +buildTeamvTeamTitle(teamPairDetails.team1, name, teamPairDetails.gender, teamPairDetails.competitionTitle)
            }
            generateHtml(teamPairDetails.competitionSubType, teamPairDetails, generateRecordsForAllOpponents)
        }

        outputStream.append(virtualFooter)
        outputStream.append("\r\n")

    }

    private fun buildTeamvTeamTitle(team1: String, team2: String, gender: String, competitionTitle: String): String {
        if (gender.isNotEmpty())
            return "${team1} v ${team2} ${gender} ${competitionTitle} Progressive Records"
        else
            return "${team1} v ${team2} ${competitionTitle} Progressive Records"
    }

    private fun DIV.generateHtml(
        matchType: String,
        teamPairDetails: ProgressiveData,
        generateRecordsForAllOpponents: Boolean = false,
    ) {


        table(classes = "numberOfMatchesTable") {
            tr {
                td {
                    +"Number of Matches"
                }
                td {
                    +"${teamPairDetails.matchDto.count}"
                }
                td {
                    +"From: "
                    +DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        .format(teamPairDetails.matchDto.startDate.toJavaLocalDateTime())
                }
                td {
                    +"To: "
                    +DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        .format(teamPairDetails.matchDto.endDate.toJavaLocalDateTime())
                }
            }
            tr {
                td {
                    +"${teamPairDetails.team1} won"
                }
                td {
                    +"${teamPairDetails.matchDto.firstTeamWins}"
                }
                td {
                }
                td {
                }
                td {

                }
            }
            tr {
                td {
                    val name = if (teamPairDetails.team2.lowercase() == "all") "Opponents" else teamPairDetails.team2
                    +"${name} won "
                }
                td {
                    +"${teamPairDetails.matchDto.firstTeamLosses}"
                }
                td {
                }
                td {
                }
                td {

                }
            }
            tr {
                td {
                    +getLabelForDrawnMatches(teamPairDetails.competitionSubType)
                }
                td(null, "colspan", "4") {
                    +"${teamPairDetails.matchDto.draws}"
                }
            }
            tr {
                td {
                    +"Ties"
                }
                td {
                    +"${teamPairDetails.matchDto.ties}"
                }
                td {
                }
                td {
                }
                td {

                }
            }
            if (teamPairDetails.matchDto.abandonedAsDraw != 0) {
                val (drawText, pluralDrawText) = if (isMatchTypeMultiInnings(matchType)) {
                    Pair("draw", "draws")
                } else {
                    Pair("no result", "no results")
                }
                tr {
                    td(null, "colspan", "5") {
                        if (teamPairDetails.matchDto.abandonedAsDraw == 1)
                            +"1 match between these teams was abandoned as a $drawText"
                        else
                            +"${teamPairDetails.matchDto.abandonedAsDraw} matches between these teams were abandoned as $pluralDrawText"
                    }
                }
            }
            if (teamPairDetails.matchDto.abandoned != 0) {
                tr {
                    td(null, "colspan", "5") {
                        if (teamPairDetails.matchDto.abandoned == 1)
                            +"1 match between these teams was abandoned"
                        else
                            +"${teamPairDetails.matchDto.abandoned} matches between these teams were abandoned"
                    }
                }
            }
            if (teamPairDetails.matchDto.cancelled != 0) {
                tr {
                    td(null, "colspan", "5") {
                        if (teamPairDetails.matchDto.cancelled == 1)
                            +"1 match between these teams was cancelled"
                        else
                            +"${teamPairDetails.matchDto.cancelled} matches between these teams were cancelled"
                    }
                }
            }
        }

        if (teamPairDetails.matchDto.count > 0) {
            for (index in 0..1) {
                if (index == 0) {
                    h4 { +teamPairDetails.team1 }
                } else {
                    h4 {
                        if (teamPairDetails.team2.lowercase() == "all")
                            +"Opponents"
                        else
                            +teamPairDetails.team2
                    }
                }
                generateSingleMatchDataTables(matchType, teamPairDetails, index, generateRecordsForAllOpponents)
                generateFowHtml(
                    teamPairDetails.bestFoW[index],
                    index == 0,
                    generateRecordsForAllOpponents
                ) { wicket, teamA, teamB ->
                    log.warn("MatchType: ${matchType}: FOW: wicket $wicket for $teamA v $teamB has unknown players")
                }
            }
        }
        p { +teamPairDetails.authors.joinToString(", ") }
        generateRecordPageFooter(teamPairDetails.team1, teamPairDetails.team2, matchType)
    }

    private fun DIV.generateFowHtml(
        fows: Map<Int, FowDetails>,
        isFirstTeam: Boolean = false,
        generateRecordsForAllOpponents: Boolean,
        report: (Int, String, String) -> Unit,
    ) {
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
                            td {
                                if (generateRecordsForAllOpponents) {
                                    if (isFirstTeam) {
                                        +"v ${fow.opponents}"
                                    } else {
                                        +fow.opponents
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
                            +"A total of ${
                                getPartnership(
                                    multiPlayerFowDao.total,
                                    multiPlayerFowDao.unbroken
                                )
                            } was added for the ${getWicket(multiPlayerFowDao.wicket)} wicket"
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

    private fun DIV.generateSingleMatchDataTables(
        matchType: String,
        teamPairDetails: ProgressiveData,
        index: Int,
        generateRecordsForAllOpponents: Boolean,
    ) {
        table(classes = "teamTotalsTable") {
            generateTeamScoreRows(
                teamPairDetails.highestScores[index],
                "Highest Team Total",
                isFirstTeam = index == 0,
                generateRecordsForAllOpponents
            )
        }
        table(classes = "teamScoresTable") {
            generateTeamScoreRows(
                teamPairDetails.lowestAllOutScores[index],
                "Lowest All-out Team Total",
                isFirstTeam = index == 0,
                generateRecordsForAllOpponents
            )
        }
        table(classes = "mostRunsInningsTable") {
            generateMostRunsInInningsRows(
                teamPairDetails.highestIndividualScores[index],
                isFirstTeam = index == 0,
                generateRecordsForAllOpponents
            )
        }
        table(classes = "bestBowlingRowsTable") {
            generateBestBowlingRows(
                teamPairDetails.bestBowlingInnings[index],
                "Best Bowling in innings (min:3 wickets)",
                isFirstTeam = index == 0,
                generateRecordsForAllOpponents
            )
        }

        table(classes = "bestBowlingRowsTable") {
            if (isMatchTypeMultiInnings(matchType)) {
                generateBestBowlingRows(
                    teamPairDetails.bestBowlingMatch[index],
                    "Best Bowling in match",
                    isFirstTeam = index == 0,
                    generateRecordsForAllOpponents
                )
            }
        }
    }


    private fun TABLE.generateTeamScoreRows(
        totals: List<TotalDto>,
        title: String,
        isFirstTeam: Boolean = true,
        generateRecordsForAllOpponents: Boolean = false,
    ) {
        if (totals.isEmpty()) {
            generateEmptyTotalRow(title)
        } else {
            totals.forEachIndexed { ndx, total ->
                generateTotalsRow(
                    ndx,
                    total,
                    isFirstTeam = isFirstTeam,
                    generateRecordsForAllOpponents = generateRecordsForAllOpponents
                ) {
                    +title
                }
            }
        }
    }

    private fun TABLE.generateMostRunsInInningsRows(
        highestScore: List<HighestScoreDto>,
        isFirstTeam: Boolean,
        generateRecordsForAllOpponents: Boolean = false,
    ) {
        if (highestScore.isEmpty()) {
            tr {
                td {
                    +"Most Runs in innings (min 25)"
                }
                td {
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
            highestScore.forEachIndexed { ndx, score ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Most Runs in innings"
                    }
                    td {
                        +getNotOutScore(score.score, score.notOut)
                    }
                    td {
                        +score.name
                    }
                    td {
                        if (generateRecordsForAllOpponents) {
                            if (isFirstTeam) {
                                +"v ${score.opponents}"
                            } else {
                                +score.opponents
                            }
                        }
                    }
                    td {
                        +score.location
                    }
                    td(null) {
                        +score.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateBestBowlingRows(
        bestBowlingInnings: List<BestBowlingDto>,
        title: String,
        isFirstTeam: Boolean = false,
        generateRecordsForAllOpponents: Boolean = false,
    ) {
        if (bestBowlingInnings.isEmpty()) {
            tr {
                td {
                    +title
                }
                td {
                }
                td {

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
                    td {
                        +"${formatOvers(bb.balls, bb.ballsPerOver)}-${bb.maidens}-${bb.runs}-${bb.wickets}"
                    }
                    td {
                        +bb.name
                    }
                    td {
                        if (generateRecordsForAllOpponents) {
                            if (isFirstTeam) {
                                +"v ${bb.opponents}"
                            } else {
                                +bb.opponents

                            }
                        }
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
        val bpo = if (ballsPerOver == 0) 6 else ballsPerOver
        val overs = balls / bpo
        val ballsLeft = balls.mod(bpo)
        return if (ballsLeft == 0) {
            overs.toString()
        } else {
            "${overs}.$ballsLeft"
        }
    }
}


fun formatDouble(input: Double, scale: Int) = String.format("%.${scale}f", input)







