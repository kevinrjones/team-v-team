package com.knowledgespike.teamvteam.html


import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.html.*
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.teamvteam.json.TeamPairDetailsData
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.time.format.DateTimeFormatter

class GenerateHtml {

    private val columnOneWidth = "130"
    private val columnTwoWidth = "130"
    private val columnFiveWidth = "80"
    private val virtualHeader = "<!--#include virtual=\"/includes/header.html\" -->"
    private val virtualFooter = "<!--#include virtual=\"/includes/footer.html\" -->"

    private val log by LoggerDelegate()

    fun generateTeamVsTeamRecordsPage(
        teamPairDetails: TeamPairDetailsData,
        file: File
    ) {

        file.parentFile.mkdirs()

        val fileWriter = file.writer()

        fileWriter.use {
            generateTeamVTeamHtml(teamPairDetails, fileWriter)
        }
        log.info("Completed: ${file.name}")

    }

    fun createTeamPairHomePages(
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

    fun generateIndexPageForTeamsAndType(
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


    // Appendable is any of
// BufferedWriter, CharArrayWriter, CharBuffer, FileWriter, FilterWriter, LogStream, OutputStreamWriter,
// PipedWriter, PrintStream, PrintWriter, StringBuffer, StringBuilder, StringWriter, Writer
    private fun generateTeamVTeamHtml(
        teamPairDetails: TeamPairDetailsData,
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
                            width: 80px;
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
            generateHtml(teamPairDetails, teamPairDetails.competitionSubType)
        }

        outputStream.append(virtualFooter)
        outputStream.append("\r\n")
    }

    private fun DIV.generateHtml(teamPairDetails: TeamPairDetailsData, matchType: String) {


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
            generateSingleMatchDataTable(teamPairDetails, matchType, index)
            generateFowHtml(teamPairDetails.bestFoW[index]) { wicket, teamA, teamB ->
                log.warn("MatchType: ${matchType}: FOW: wicket $wicket for $teamA vs $teamB has unknown players")
            }
            generateOverallDataTable(teamPairDetails, index)
        }

        if (getAnyPossibleInvalid(teamPairDetails))
            generateMessageRow()
        p { +teamPairDetails.authors.joinToString(", ") }

        generateRecordPageFooter(teamPairDetails.team1, teamPairDetails.team2, matchType)
    }

    private fun getAnyPossibleInvalid(teamPairDetails: TeamPairDetailsData): Boolean {
        val size =
            teamPairDetails.bestFoW.flatMap { it.values }.flatMap { it.standardFow }.filter { it.possibleInvalid }.size
        return size != 0
    }

    private fun DIV.generateOverallDataTable(teamPairDetails: TeamPairDetailsData, index: Int) {
        generateOverallMostRuns(teamPairDetails, index)
        generateOverallMostWickets(teamPairDetails, index)
        generateOverallMostCatches(teamPairDetails.mostCatchesVsOpposition[index], "Catches")
        generateOverallMostCatches(teamPairDetails.mostStumpingsVsOpposition[index], "Stumpings")
    }

    private fun DIV.generateOverallMostRuns(
        teamPairDetails: TeamPairDetailsData,
        index: Int
    ) {
        table {
            thead {
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Most Runs"
                    }
                    td(null, "width", columnTwoWidth) {
                    }
                    td(null) {

                    }
                    td {

                    }
                    td(null, "width", columnFiveWidth) {
                    }
                    td {}
                    td {}
                    td {}
                    td {}
                }
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Name"
                    }
                    td(null, "width", columnTwoWidth) {
                        +"Matches"
                    }
                    td(null) {
                        +"Innings"
                    }
                    td {
                        +"Not Outs"
                    }
                    td(null, "width", columnFiveWidth) {
                        +"Runs"
                    }
                    td(null, "width", columnFiveWidth) {
                        +"HS"
                    }
                    td {
                        +"Average"
                    }
                }
            }

            teamPairDetails.mostRunsVsOpposition[index].forEach {
                tr {
                    td(null, "width", columnOneWidth) { +it.name }
                    td { +"${it.matches}" }
                    td { +"${it.innings}" }
                    td { +"${it.notOuts}" }
                    td { +"${it.runs}" }
                    td { +it.hs }
                    td { +formatDouble(it.average, 2) }
                }
            }
        }
    }

    private fun DIV.generateOverallMostWickets(
        teamPairDetails: TeamPairDetailsData,
        index: Int
    ) {
        table {
            thead {
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Most Wickets"
                    }
                    td(null, "width", columnTwoWidth) {
                    }
                    td {

                    }
                    td {

                    }
                    td {
                    }
                    td {}
                    td {}
                    td {}
                }
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Name"
                    }
                    td(null, "width", columnTwoWidth) {
                        +"Matches"
                    }
                    td {
                        +"Balls"
                    }
                    td {
                        +"Maidens"
                    }
                    td {
                        +"Runs"
                    }
                    td {
                        +"Wickets"
                    }
                    td {
                        +"Average"
                    }
                    td {
                        +"BB"
                    }
                }
            }

            teamPairDetails.mostWicketsVsOpposition[index].forEach {
                tr {
                    td(null, "width", columnOneWidth) { +it.name }
                    td(null, "width", columnTwoWidth) { +"${it.matches}" }
                    td { +"${it.balls}" }
                    td { +"${it.maidens}" }
                    td { +"${it.runs}" }
                    td { +"${it.wickets}" }
                    td { +formatDouble(it.average, 2) }
                    td { +getBestBowling(it) }
                }
            }
        }
    }

    private fun DIV.generateOverallMostCatches(
        mostDismissals: List<MostDismissalsDto>,
        title: String
    ) {
        table {
            thead {
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Most ${title}"
                    }
                    td(null, "width", columnTwoWidth) { }
                    td { }
                }
                tr {
                    td(null, "width", columnOneWidth) {
                        +"Name"
                    }
                    td(null, "width", columnTwoWidth) {
                        +"Matches"
                    }
                    td {
                        +title
                    }
                }
            }

            if (mostDismissals.isEmpty()) {
                tr {
                    td(null, "width", columnOneWidth) { +"-" }
                    td(null, "width", columnTwoWidth) { +"-" }
                    td { +"-" }
                }
            } else {

                mostDismissals.forEach {
                    tr {
                        td { +it.name }
                        td(null, "width", columnTwoWidth) { +"${it.matches}" }
                        td { +"${it.dismissals}" }
                    }
                }
            }
        }
    }

    private fun getBestBowling(mostWicketsDto: MostWicketsDto): String {
        return "${mostWicketsDto.bbwickets}/${mostWicketsDto.bbruns}"
    }

    private fun DIV.generateSingleMatchDataTable(
        teamPairDetails: TeamPairDetailsData,
        matchType: String,
        index: Int
    ) {
        table {
            generateHighestScoreRow(teamPairDetails.highestScores[index])
            generateLowestScoreRow(
                teamPairDetails.teamAllLowestScores[index].lowestAllOutScore,
                teamPairDetails.teamAllLowestScores[index].lowestCompleteScores
            )
            generateBattingRows(
                this,
                teamPairDetails.highestIndividualScore[index],
                teamPairDetails.highestIndividualStrikeRates[index],
                teamPairDetails.highestIndividualStrikeRatesWithLimit[index],
                teamPairDetails.lowestIndividualStrikeRates[index],
                teamPairDetails.lowestIndividualStrikeRatesWithLimit[index],
                teamPairDetails.strikeRateScoreLimit,
                teamPairDetails.strikeRateLowerBallsLimit,
                teamPairDetails.strikeRateUpperBallsLimit,
                teamPairDetails.mostBoundaries[index],
                teamPairDetails.mostSixes[index],
                teamPairDetails.mostFours[index]
            )
            generateBowlingRows(
                this,
                teamPairDetails.bestBowlingInnings[index],
                teamPairDetails.bestBowlingSRInnings[index],
                teamPairDetails.bestBowlingSRWithLimitInnings[index],
                teamPairDetails.bestBowlingERInnings[index],
                teamPairDetails.bestBowlingERWithLimitInnings[index],
                teamPairDetails.worstBowlingERInnings[index],
                teamPairDetails.worstBowlingERWithLimitInnings[index],
                teamPairDetails.economyRateOversLimit
            )

            if (isMatchTypeMultiInnings(matchType)) {
                generateBestBowlingInMatchRow(teamPairDetails.bestBowlingMatch[index])
            }
        }
    }

    private fun generateBowlingRows(
        table: TABLE,
        bestBowlingInnings: List<BestBowlingDto>,
        bestBowlingSRInnings: List<BowlingRatesDto>,
        bestBowlingSRWithLimitInnings: List<BowlingRatesDto>,
        bestBowlingERInnings: List<BowlingRatesDto>,
        bestBowlingERWithQualificationInnings: List<BowlingRatesDto>,
        worstBowlingERInnings: List<BowlingRatesDto>,
        worstBowlingERWithLimitInnings: List<BowlingRatesDto>,
        bestBowlingOversLimit: Int,
    ) {
        table.generateBestBowlingInInningsRow(bestBowlingInnings)
        if (bestBowlingSRInnings.isNotEmpty()) {
            table.generateBestBowlingSRInInningsRow("Best Bowling SR (no qualification)", bestBowlingSRInnings)
            table.generateBestBowlingSRInInningsRow(
                "Best Bowling SR (min: $bestBowlingOversLimit overs)",
                bestBowlingSRWithLimitInnings
            )
        }
        if (bestBowlingERInnings.isNotEmpty()) {
            table.generateBestBowlingEconRateInInningsRow("Best Economy Rate (no qualification)", bestBowlingERInnings)
            table.generateBestBowlingEconRateInInningsRow(
                "Best Economy Rate (min: $bestBowlingOversLimit overs)",
                bestBowlingERWithQualificationInnings
            )
        }
        if (worstBowlingERInnings.isNotEmpty()) {
            table.generateBestBowlingEconRateInInningsRow("Worst Economy Rate", worstBowlingERInnings)
            table.generateBestBowlingEconRateInInningsRow(
                "Worst Economy Rate (min: $bestBowlingOversLimit overs)",
                worstBowlingERWithLimitInnings
            )
        }
    }

    private fun generateBattingRows(
        table: TABLE,
        highestIndividualScores: List<HighestScoreDto>,
        highestIndividualStrikeRates: List<StrikeRateDto>,
        highestIndividualStrikeRatesWithLimit: List<StrikeRateDto>,
        lowestIndividualStrikeRates: List<StrikeRateDto>,
        lowestIndividualStrikeRatesWithLimit: List<StrikeRateDto>,
        strikeRateRunsLimit: Int,
        strikeRateLowerBallsLimit: Int,
        strikeRateUpperBallsLimit: Int,
        highestIndividualBoundaries: List<BoundariesDto>,
        highestIndividualSixes: List<BoundariesDto>,
        highestIndividualFours: List<BoundariesDto>
    ) {
        table.generateMostRunsInInningsRow(highestIndividualScores)
        if (highestIndividualStrikeRates.isNotEmpty()) {
            table.generateHighestStrikeRateRow(
                highestIndividualStrikeRates,
                "Highest Strike Rate in innings (no qualification)"
            )
            table.generateHighestStrikeRateRow(
                highestIndividualStrikeRatesWithLimit,
                "Highest Strike Rate in innings (min: $strikeRateRunsLimit runs)"
            )
            table.generateHighestStrikeRateRow(
                lowestIndividualStrikeRates,
                "Lowest Strike Rate in innings (min: $strikeRateLowerBallsLimit balls)"
            )
            table.generateHighestStrikeRateRow(
                lowestIndividualStrikeRatesWithLimit,
                "Lowest Strike Rate in innings (min: $strikeRateUpperBallsLimit balls)"
            )
        }
        if (highestIndividualBoundaries.isNotEmpty()) {
            table.generateHighestBoundariesRow("Boundaries", highestIndividualBoundaries)
        }
        if (highestIndividualSixes.isNotEmpty()) {
            table.generateHighestBoundariesRow("Sixes", highestIndividualSixes)
        }
        if (highestIndividualFours.isNotEmpty()) {
            table.generateHighestBoundariesRow("Fours", highestIndividualFours)
        }
    }

    private fun TABLE.generateBestBowlingInMatchRow(bestBowlingMatch: List<BestBowlingDto>) {
        if (bestBowlingMatch.isEmpty()) {
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
                    td(null, "width", columnTwoWidth) {
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

    private fun TABLE.generateBestBowlingInInningsRow(bestBowlingInnings: List<BestBowlingDto>) {
        if (bestBowlingInnings.isEmpty()) {
            tr {
                td {
                    +"Best Bowling in innings"
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
            bestBowlingInnings.forEachIndexed { ndx, bb ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Best Bowling in innings"
                    }
                    td(null, "width", columnTwoWidth) {
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

    private fun TABLE.generateBestBowlingSRInInningsRow(
        title: String,
        bestBowlingSRInnings: List<BowlingRatesDto>
    ) {
        if (bestBowlingSRInnings.size == 0) {
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
            }
        } else {
            bestBowlingSRInnings.forEachIndexed { ndx, bb ->
                tr {
                    td {
                        if (ndx == 0)
                            +title
                    }
                    td(null, "width", columnTwoWidth) {
                        +"${formatDouble(bb.sr, 2)} (${bb.balls}-${bb.maidens}-${bb.runs}-${bb.wickets})"
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

    private fun TABLE.generateBestBowlingEconRateInInningsRow(
        title: String,
        bestBowlingERInnings: List<BowlingRatesDto>
    ) {
        if (bestBowlingERInnings.size == 0) {
            tr {
                td {
                    +"Best Economy Rate"
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
            bestBowlingERInnings.forEachIndexed { ndx, bb ->
                tr {
                    td {
                        if (ndx == 0)
                            +title
                    }
                    td(null, "width", columnTwoWidth) {
                        +"${formatDouble(bb.sr, 2)} (${bb.overs}-${bb.maidens}-${bb.runs}-${bb.wickets})"
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

    private fun TABLE.generateHighestStrikeRateRow(strikeRates: List<StrikeRateDto>, title: String) {
        if (strikeRates.size == 0) {
            tr {
                td {
                    +" $title"
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
            strikeRates.forEachIndexed { ndx, sr ->
                tr {
                    td {
                        if (ndx == 0)
                            +"$title"
                    }
                    td(null, "width", columnTwoWidth) {
                        +"${formatDouble(sr.strikeRate, 2)} (${sr.runs} off ${sr.balls})"
                    }
                    td {
                        +sr.name
                    }
                    td(null) {
                        +sr.location
                    }
                    td(null) {
                        +sr.seriesDate
                    }
                }
            }
        }
    }

    private fun TABLE.generateHighestBoundariesRow(
        title: String,
        boundaries: List<BoundariesDto>
    ) {
        if (boundaries.isEmpty() || boundaries[0].boundaries == 0) {
            tr {
                td {
                    +"Most ${title} in innings"
                }
                td(null, "width", columnTwoWidth) {
                    +"0"
                }
                td {

                }
                td {
                }
                td {
                }
            }
        } else {
            boundaries.forEachIndexed { ndx, sr ->
                tr {
                    td {
                        if (ndx == 0)
                            +"Most ${title} in innings"
                    }
                    td(null, "width", columnTwoWidth) {
                        +getBoundaries(sr)
                    }
                    td {
                        +sr.name
                    }
                    td(null) {
                        +sr.location
                    }
                    td(null) {
                        +sr.seriesDate
                    }
                }
            }
        }
    }

    private fun getBoundaries(boundaries: BoundariesDto): String {
        if (boundaries.fours == 0 && boundaries.sixes == 0) return "${boundaries.boundaries}"
        return "${boundaries.boundaries} (${boundaries.fours} 4s, ${boundaries.sixes} 6s)"
    }

    private fun TABLE.generateHighestScoreRow(totals: List<TotalDto>) {
        if (totals.isEmpty()) {
            generateEmptyTotalRow("Highest Team Total", columnTwoWidth, columnFiveWidth)
        } else {
            totals.forEachIndexed { ndx, total ->
                generateTotalsRow(ndx, total, columnTwoWidth, columnFiveWidth) {
                    +"Highest Team Total"
                }
            }
        }

    }

    private fun TABLE.generateLowestScoreRow(allOutTotals: List<TotalDto>, completeTotals: List<TotalDto>) {
        if (allOutTotals.isEmpty() && completeTotals.isEmpty()) {
            generateEmptyTotalRow("Lowest All-Out Team Total", columnTwoWidth, columnFiveWidth)

        } else if (allOutTotals.isNotEmpty()) {
            allOutTotals.forEachIndexed { ndx, total ->
                generateTotalsRow(ndx, total, columnTwoWidth, columnFiveWidth) {
                    +"Lowest All Out Team Total"
                }
            }
        } else {
            completeTotals.forEachIndexed { ndx, total ->
                generateTotalsRow(ndx, total, columnTwoWidth, columnFiveWidth) {
                    +"Lowest "
                    b {
                        +"Completed"
                    }
                    +" Team Total"
                }
            }
        }
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
                                    +getWicketDetails(wicket, fow.possibleInvalid)
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

    private fun DIV.generateMessageRow() {
        p {
            +"\u2020 The partnership record for the marked wickets may have been exceeded in other matches for which we don't have fall of wickets data."
        }
    }
}


fun formatDouble(input: Double, scale: Int) = String.format("%.${scale}f", input)






