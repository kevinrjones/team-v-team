package com.knowledgespike.teamvteam

import com.knowledgespike.db.tables.references.TEAMS
import com.knowledgespike.db.tables.references.TEAMSMATCHTYPES
import com.knowledgespike.extensions.generateFileName
import com.knowledgespike.teamvteam.data.Competition
import com.knowledgespike.teamvteam.data.OpponentWithAuthors
import com.knowledgespike.teamvteam.data.Team
import com.knowledgespike.teamvteam.database.ProcessTeams
import com.knowledgespike.teamvteam.html.GenerateHtml
import com.knowledgespike.teamvteam.json.TeamPairDetailsData
import com.knowledgespike.teamvteam.json.TeamPairHomePagesJson
import com.knowledgespike.teamvteam.json.writeJsonMatchData
import com.knowledgespike.teamvteam.json.writeJsonTeamPairPageData
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.apache.commons.cli.*
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.system.exitProcess

typealias TeamNameToIds = Map<String, List<Int>>

class Application {
    companion object {
        private val log by LoggerDelegate()

        var dialect = SQLDialect.DEFAULT

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {

            try {
                val options = createCommandLineOptions()

                val formatter = HelpFormatter()

                if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
                    formatter.printHelp(
                        200,
                        "java com.knowledgespike.teamvteam.ApplicationKt",
                        "Run the team vs team code",
                        options,
                        "",
                        true
                    )
                    exitProcess(0)
                }


                val cmd: CommandLine
                val parser: CommandLineParser = DefaultParser()
                try {
                    cmd = parser.parse(options, args)
                } catch (e: Exception) {
                    println(e.message)
                    formatter.printHelp(
                        200,
                        "java com.knowledgespike.teamvteam.ApplicationKt",
                        "Run the team vs team code",
                        options,
                        "",
                        true
                    )
                    exitProcess(2)
                }


                val connectionString = cmd.getOptionValue("c")
                val userName = cmd.getOptionValue("u")
                val password = cmd.getOptionValue("p")
                val baseDirectory = cmd.getOptionValue("bd")
                val fullyQualifiedHtmlOutputDirectory = cmd.getOptionValue("ho")
                val relativeJsonOutputDirectory = cmd.getOptionValue("jo")
                val dialectOption = cmd.getOptionValue("d")

                dialect = when (dialectOption) {
                    "mariadb" -> {
                        SQLDialect.MARIADB
                    }

                    "mysql" -> {
                        SQLDialect.MYSQL
                    }

                    "postgres" -> {
                        SQLDialect.POSTGRES
                    }

                    else -> {
                        SQLDialect.DEFAULT
                    }
                }

                val jsonOutputDirectory = "$baseDirectory$relativeJsonOutputDirectory"
                processAllCompetitions(
                    baseDirectory,
                    fullyQualifiedHtmlOutputDirectory,
                    jsonOutputDirectory,
                    connectionString,
                    userName,
                    password
                )
            } catch (t: Throwable) {
                log.error("Error in main", t)
            }
        }

        private suspend fun processAllCompetitions(
            baseDirectory: String,
            htmlOutputDirectory: String,
            jsonOutputDirectory: String,
            connectionString: String,
            userName: String,
            password: String
        ) {

            withContext(Dispatchers.IO) {
                val dataDirectory = "$baseDirectory/data"
                val allCompetitions = getAllCompetitions(dataDirectory)

                val jobs = mutableListOf<Job>()

                allCompetitions.forEach { competition: Competition ->
                    val job = launch {

                        val matchDesignator = competition.title
                        val matchSubType = competition.subType

                        var pairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()


                        val competitionWithSortedTeams =
                            competition.copy(teams = competition.teams.sortedBy { it.team })

                        val teamsWithDuplicates =
                            getTeamIds(
                                connectionString,
                                userName,
                                password,
                                competitionWithSortedTeams.teams,
                                matchSubType
                            )

                        val opponentsForTeam = mutableMapOf<String, TeamNameToIds>()
                        competition.teams.forEach {
                            val opponents: TeamNameToIds =
                                getTeamIdsForTeamNames(connectionString, userName, password, it.opponents, matchSubType)
                            opponentsForTeam.put(it.team, opponents)
                        }

                        val opponentsWithAuthors = competition.teams
                            .filter { it.authors.isNotEmpty() }
                            .map { OpponentWithAuthors(it.team, it.authors) }
                            .associateBy({ it.team }, { it.author })


                        val processTeams = ProcessTeams(teamsWithDuplicates, opponentsForTeam, opponentsWithAuthors)

                        processTeams.process(
                            connectionString,
                            userName,
                            password,
                            matchSubType,
                            "$jsonOutputDirectory/${competition.outputDirectory}"
                        ) { teamPairDetails, jsonDirectory ->

                            log.debug(
                                "Updating data for {} and {} for {}",
                                teamPairDetails.teams[0],
                                teamPairDetails.teams[1],
                                matchDesignator
                            )

                            val teamPairDetailsData = TeamPairDetailsData(
                                teamPairDetails.teams[0],
                                teamPairDetails.teams[1],
                                competition.title,
                                competition.subType,
                                teamPairDetails.matchDto,
                                teamPairDetails.authors,
                                teamPairDetails.highestScores,
                                teamPairDetails.highestIndividualScore,
                                teamPairDetails.highestIndividualStrikeRates,
                                teamPairDetails.highestIndividualStrikeRatesWithLimit,
                                teamPairDetails.lowestIndividualStrikeRates,
                                teamPairDetails.lowestIndividualStrikeRatesWithLimit,
                                teamPairDetails.mostFours,
                                teamPairDetails.mostSixes,
                                teamPairDetails.mostBoundaries,
                                teamPairDetails.bestBowlingInnings,
                                teamPairDetails.bestBowlingMatch,
                                teamPairDetails.bestBowlingSRInnings,
                                teamPairDetails.bestBowlingSRWithLimitInnings,
                                teamPairDetails.bestBowlingERInnings,
                                teamPairDetails.bestBowlingERWithLimitInnings,
                                teamPairDetails.worstBowlingERInnings,
                                teamPairDetails.worstBowlingERWithLimitInnings,
                                teamPairDetails.bestFoW,
                                teamPairDetails.mostRunsVsOpposition,
                                teamPairDetails.mostWicketsVsOpposition,
                                teamPairDetails.mostCatchesVsOpposition,
                                teamPairDetails.mostStumpingsVsOpposition,
                                teamPairDetails.teamAllLowestScores,
                                Clock.System.now()
                            )


                            val fileName = teamPairDetails.generateFileName(matchSubType)

                            writeJsonMatchData(jsonDirectory, fileName, teamPairDetailsData)
                            pairsForPage = addPairToCollectionForPairVPairPages(
                                competition.teams.map { it.team },
                                teamPairDetailsData,
                                pairsForPage
                            )
                        }


                        val jsonDirectory = "$jsonOutputDirectory/${competition.outputDirectory}"
                        createTeamPairHomePagesData(
                            matchSubType,
                            competition.title,
                            pairsForPage,
                            jsonDirectory
                        )
                    }
                    jobs.add(job)
                }
                jobs.forEach { j -> j.join() }
                log.info("processAllDirectories finished")

                generateHtmlIndexAndTeamPagesForAllCompetitions(baseDirectory, htmlOutputDirectory)
                generateHtmlTeamPairHomePagesForAllCompetitions(jsonOutputDirectory, htmlOutputDirectory)
                generateHtmlFromJson(jsonOutputDirectory, htmlOutputDirectory)

            }
        }

        private fun createTeamPairHomePagesData(
            matchSubType: String,
            title: String,
            pairsForPage: MutableMap<String, TeamPairHomePagesData>,
            jsonDirectory: String
        ) {
            pairsForPage.filter { it.value.shouldHaveOwnPage }.forEach { (teamName, teamPairHomePagesData) ->

                log.debug("createTeamPairHomePages for: {}", teamName)
                val fileName =
                    "${jsonDirectory}/${teamName.replace(" ", "_")}_${matchSubType}.json"
                val file = File(fileName)
                log.debug("createTeamPairHomePages fileName: {}", fileName)
                file.parentFile.mkdirs()

                val names = teamPairHomePagesData.teamPairDetails.map { Pair(it.team1, it.team2) }
                val teamPairHomePagesDataJson = TeamPairHomePagesJson(teamName, names, title, matchSubType)

                writeJsonTeamPairPageData(fileName, teamPairHomePagesDataJson)
            }
        }


        private fun getAllCompetitions(dataDirectory: String): List<Competition> {
            val allCompetitions = mutableListOf<Competition>()
            Files.list(Paths.get(dataDirectory))
                .filter { it.isRegularFile() }
                .filter {
                    val fileName = it.fileName.toString()
                    val ret = fileName.endsWith("json")

                    ret
                }.forEach {
                    val file = it.toFile()
                    val data: String = file.readText()

                    allCompetitions.addAll(Json.decodeFromString<List<Competition>>(data))
                }
            return allCompetitions
        }

        private fun generateHtmlFromJson(
            jsonOutputDirectory: String,
            htmlOutputDirectory: String
        ) {
            val jsonDirectory = Paths.get(jsonOutputDirectory)

            val matchSubtypeDirectories =
                Files.list(jsonDirectory).filter { it.isDirectory() }.sorted().toList()

            val recordPage = GenerateHtml()

            matchSubtypeDirectories.forEach { path ->
                val name = path.name
                val jsonFiles =
                    Files.list(path).filter { it.isRegularFile() }.filter { it.name.contains("_v_") }.sorted().toList()
                jsonFiles.forEach { jsonFile ->
                    val details = getTvTJsonData(jsonFile.toString())

                    if (details != null) {
                        val htmlFileName = jsonFile.name.replace(".json", ".html")

                        val htmlFullName = "$htmlOutputDirectory/${name}/$htmlFileName"
                        val file = File(htmlFullName)

                        recordPage.generateTeamVsTeamRecordsPage(
                            details,
                            file
                        )
                    }

                }
            }
        }

        private fun generateHtmlIndexAndTeamPagesForAllCompetitions(
            baseDirectory: String,
            htmlOutputDirectory: String
        ) {
            val dataDirectory = "$baseDirectory/data"
            val allCompetitions = getAllCompetitions(dataDirectory)

            allCompetitions.forEach { competition: Competition ->
                val recordPage = GenerateHtml()
                val outputDirectory = "${htmlOutputDirectory}/${competition.outputDirectory}"
                val gender = competition.gender
                val country = competition.country
                val sortedTeamNames: List<String> = competition.teams.map { it.team }.sorted()

                recordPage.generateIndexPageForTeamsAndType(
                    sortedTeamNames,
                    competition.subType,
                    country,
                    gender,
                    competition.title,
                    outputDirectory,
                    competition.extraMessages
                )

            }
        }

        private fun generateHtmlTeamPairHomePagesForAllCompetitions(
            jsonOutputDirectory: String,
            htmlOutputDirectory: String
        ) {
            val jsonDirectory = Paths.get(jsonOutputDirectory)

            val matchSubtypeDirectories =
                Files.list(jsonDirectory).filter { it.isDirectory() }.sorted().toList()

            val recordPage = GenerateHtml()

            matchSubtypeDirectories.forEach { path ->
                val name = path.name
                val jsonFiles =
                    Files.list(path).filter { it.isRegularFile() }.filter { !it.name.contains("_v_") }.sorted().toList()
                jsonFiles.forEach { jsonFile ->
                    val details = getHomePageJsonData(jsonFile.toString())

                    if (details != null) {
                        val htmlFileName = jsonFile.name.replace(".json", ".html")

                        val htmlFullName = "$htmlOutputDirectory/${name}/$htmlFileName"
                        val file = File(htmlFullName)

                        recordPage.createTeamPairHomePages(details, file)
                    }
                }
            }
        }


        private fun addPairToCollectionForPairVPairPages(
            competitionTeams: List<String>,
            teamPair: TeamPairDetailsData,
            pairsForPage: Map<String, TeamPairHomePagesData>
        ): MutableMap<String, TeamPairHomePagesData> {
            val mutablePairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()
            mutablePairsForPage.putAll(pairsForPage)
            var teamName = teamPair.team1
            var hasOwnPage = competitionTeams.contains(teamName)

            var pairEx = pairsForPage[teamName]
            if (pairEx == null) {
                val teamPairList = mutableListOf<TeamPairDetailsData>()
                teamPairList.add(teamPair)
                mutablePairsForPage[teamPair.team1] = TeamPairHomePagesData(hasOwnPage, teamPairList)
            } else {
                pairEx.teamPairDetails.add(teamPair)
            }
            teamName = teamPair.team2
            pairEx = pairsForPage[teamName]
            if (pairEx == null) {
                hasOwnPage = competitionTeams.contains(teamName)
                val teamPairList = mutableListOf<TeamPairDetailsData>()
                teamPairList.add(teamPair)
                mutablePairsForPage[teamPair.team2] = TeamPairHomePagesData(hasOwnPage, teamPairList)
            } else {
                pairEx.teamPairDetails.add(teamPair)
            }
            return mutablePairsForPage
        }

        private fun getTeamIds(
            connectionString: String,
            userName: String,
            password: String,
            teams: List<Team>,
            matchType: String
        ): TeamNameToIds {


            val teamNameAndIds = mutableMapOf<String, List<Int>>()
            DriverManager.getConnection(connectionString, userName, password).use { conn ->
                val context = DSL.using(conn, dialect)

                for (t in teams) {
                    val ids = mutableListOf<Int>()
                    val team = t.team.trim()
                    if (team.isNotEmpty()) {
                        val teamIds = getTeamIdsFrom(context, team, matchType)

                        ids.addAll(teamIds)
                        t.duplicates.forEach { duplicate ->
                            val duplicateTeamIds = getTeamIdsFrom(context, duplicate, matchType)
                            ids.addAll(duplicateTeamIds)
                        }
                    }
                    teamNameAndIds[team] = ids
                }

            }

            return teamNameAndIds
        }

        private fun getTeamIdsForTeamNames(
            connectionString: String,
            userName: String,
            password: String,
            teams: List<String>,
            matchType: String
        ): TeamNameToIds {


            val teamNameAndIds = mutableMapOf<String, List<Int>>()
            DriverManager.getConnection(connectionString, userName, password).use { conn ->
                val context = DSL.using(conn, dialect)

                for (t in teams) {
                    val ids = mutableListOf<Int>()
                    val team = t.trim()
                    if (team.isNotEmpty()) {
                        val teamIds = getTeamIdsFrom(context, team, matchType)

                        ids.addAll(teamIds)
                    }
                    teamNameAndIds[team] = ids
                }

            }

            return teamNameAndIds
        }

        private fun getTeamIdsFrom(context: DSLContext, team: String, matchType: String): List<Int> {
            val idRecord = context
                .select(TEAMS.ID)
                .from(TEAMS)
                .join(TEAMSMATCHTYPES).on(TEAMSMATCHTYPES.TEAMID.eq(TEAMS.ID))
                .where(TEAMS.NAME.eq(team))
                .and(TEAMSMATCHTYPES.MATCHTYPE.eq(matchType))
                .fetch()

            return idRecord.getValues(TEAMS.ID, Int::class.java)
        }

        private fun createCommandLineOptions(): Options {
            val options = Options()

            options.addOption("h", "help", false, "print this message")

            val connectionStringOption = Option
                .builder("c")
                .hasArg()
                .desc("database connection string")
                .argName("connection string")
                .longOpt("connectionString")
                .required()
                .build()

            val userStringOption = Option
                .builder("u")
                .hasArg()
                .desc("database user")
                .argName("user name")
                .longOpt("userName")
                .required()
                .build()

            val passwordOption = Option
                .builder("p")
                .hasArg()
                .desc("database password")
                .argName("password")
                .longOpt("password")
                .required()
                .build()

            val baseDirectoryOption = Option
                .builder("bd")
                .hasArg()
                .desc("base directory for input data files (.../team-by-team/data)")
                .argName("base directory")
                .longOpt("baseDirectory")
                .required()
                .build()

            val htmlOutputLocationOption = Option
                .builder("ho")
                .hasArg()
                .desc("the fully qualified HTML output directory")
                .argName("output directory name")
                .longOpt("htmlOutputDirectory")
                .required()
                .build()

            val jsonOutputLocationOption = Option
                .builder("jo")
                .hasArg()
                .desc("the relative JSON output directory")
                .argName("output directory name")
                .longOpt("jsonOutputDirectory")
                .required()
                .build()


            val sqlDialectOption = Option
                .builder("d")
                .hasArg()
                .desc("the JOOQ SQL dialect to use (either 'mysql' or 'postgres'")
                .argName("JOOQ SQL dialect")
                .longOpt("dialect")
                .required()
                .build()


            options.addOption(connectionStringOption)
            options.addOption(userStringOption)
            options.addOption(passwordOption)
            options.addOption(baseDirectoryOption)
            options.addOption(htmlOutputLocationOption)
            options.addOption(jsonOutputLocationOption)
            options.addOption(sqlDialectOption)

            return options
        }
    }

}

fun getTvTJsonData(jsonDirectory: String, fileName: String): TeamPairDetailsData? {
    return getTvTJsonData("${jsonDirectory}/$fileName")
}

private fun getTvTJsonData(fileName: String): TeamPairDetailsData? {
    val file: File = Path(fileName).toFile()

    if (!file.exists())
        return null

    val data: String
    data = file.readText()

    val details: TeamPairDetailsData = Json.decodeFromString(data)
    return details
}

private fun getHomePageJsonData(fileName: String): TeamPairHomePagesJson? {
    val file: File = Path(fileName).toFile()

    if (!file.exists())
        return null

    val data: String
    data = file.readText()

    val details: TeamPairHomePagesJson = Json.decodeFromString(data)
    return details
}



