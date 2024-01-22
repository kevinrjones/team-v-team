package com.knowledgespike.teamvteam

import com.knowledgespike.extensions.generateFileName
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.data.createTeamPairHomePagesData
import com.knowledgespike.shared.data.getHomePageJsonData
import com.knowledgespike.shared.data.getIndexPageJsonData
import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.shared.database.getTeamIds
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.database.ProcessTeams
import com.knowledgespike.teamvteam.html.GenerateHtml
import com.knowledgespike.teamvteam.json.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import org.apache.commons.cli.*
import org.jooq.SQLDialect
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.system.exitProcess


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

                val databaseConnection = DatabaseConnection(userName, password, connectionString, dialect)

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

                val jsonOutputDirectory = "$baseDirectory/$relativeJsonOutputDirectory"
                processAllCompetitions(
                    baseDirectory,
                    fullyQualifiedHtmlOutputDirectory,
                    jsonOutputDirectory,
                    databaseConnection
                )
            } catch (t: Throwable) {
                log.error("Error in main", t)
            }
        }

        private suspend fun processAllCompetitions(
            baseDirectory: String,
            htmlOutputDirectory: String,
            jsonOutputDirectory: String,
            databaseConnection: DatabaseConnection
        ) {

            val dataDirectory = "$baseDirectory/shared/data"
            val allCompetitions = getAllCompetitions(dataDirectory)


            withContext(Dispatchers.IO) {
                val jobs = mutableListOf<Job>()

                allCompetitions.forEach { competition: Competition ->
                    val job = launch {
                        val matchDesignator = competition.title
                        val matchSubType = competition.subType

                        val competitionWithSortedTeams =
                            competition.copy(teams = competition.teams.sortedBy { it.team })


                        val teamsWithDuplicates: TeamNameToIds =
                            getTeamIds(
                                databaseConnection,
                                competitionWithSortedTeams.teams,
                                matchSubType,
                                dialect
                            )

                        val opponentsForTeam = mutableMapOf<String, TeamNameToIds>()
                        competition.teams.forEach {
                            val opponents: TeamNameToIds =
                                getTeamIds(
                                    databaseConnection,
                                    it.opponents,
                                    matchSubType,
                                    dialect
                                )
                            opponentsForTeam[it.team] = opponents
                        }

                        val opponentsWithAuthors = competition.teams
                            .filter { it.authors.isNotEmpty() }
                            .map { TeamWithAuthors(it.team, it.authors) }
                            .associateBy({ it.team }, { it.author })


                        val processTeams =
                            ProcessTeams(teamsWithDuplicates, opponentsForTeam, opponentsWithAuthors)

                        val pairsForPage = processTeams.process(
                            databaseConnection,
                            matchSubType,
                            "$jsonOutputDirectory/${competition.outputDirectory}",
                            competition.teams.map { it.team }
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
                        }


                        val jsonDirectory = "$jsonOutputDirectory/${competition.outputDirectory}"

                        val teamNamesForIndexPage = createTeamPairHomePagesData(
                            matchSubType,
                            competition.title,
                            pairsForPage,
                            jsonDirectory
                        )

                        if (teamNamesForIndexPage.isNotEmpty()) {
                            generateIndexPageData(
                                teamNamesForIndexPage,
                                matchSubType,
                                competition.country,
                                competition.gender,
                                competition.title,
                                competition.extraMessages,
                                jsonDirectory
                            )
                        }
                    }
                    jobs.add(job)
                }
                jobs.forEach { j -> j.join() }
                log.info("process all JSON finished")
            }

            generateHtmlIndexAndTeamPagesForAllCompetitions(
                jsonOutputDirectory,
                jsonOutputDirectory,
                htmlOutputDirectory
            )
            generateHtmlTeamPairHomePagesForAllCompetitions(
                jsonOutputDirectory,
                jsonOutputDirectory,
                htmlOutputDirectory
            )
            generateHtmlFromJson(jsonOutputDirectory, jsonOutputDirectory, htmlOutputDirectory)
            log.info("process all HTML finished")

        }


        private fun generateIndexPageData(
            teamNames: List<String>,
            matchSubType: String,
            country: String,
            gender: String,
            title: String,
            extraMessages: List<String>,
            jsonDirectory: String
        ) {
            if (teamNames.isNotEmpty()) {
                val fileName =
                    "${jsonDirectory}/index.json"

                val file = File(fileName)
                log.debug("createTeamPairHomePages index fileName: {}", fileName)
                file.parentFile.mkdirs()

                writeJsonTeamPairPageIndexData(
                    fileName,
                    CompetitionIndexPage(teamNames, matchSubType, country, gender, title, extraMessages)
                )
            }
        }


        private fun generateHtmlFromJson(
            jsonDirectoryBaseName: String,
            jsonDirectoryName: String,
            htmlOutputDirectoryName: String
        ) {
            val jsonDirectory = Paths.get(jsonDirectoryName)

            val streamOfSubdirectories = Files.list(jsonDirectory)


            val matchSubtypeDirectories =
                streamOfSubdirectories.filter { it.isDirectory() }.sorted().toList()

            streamOfSubdirectories.close()
            matchSubtypeDirectories.forEach {
                generateHtmlFromJson(jsonDirectoryBaseName, it.toString(), htmlOutputDirectoryName)
            }

            val recordPage = GenerateHtml()


            val fqn = jsonDirectory.toString()
            val fqnHtmlName = fqn.replace(jsonDirectoryBaseName, htmlOutputDirectoryName)
            val jsonFiles =
                Files.list(jsonDirectory).filter { it.isRegularFile() }.filter { it.name.contains("_v_") }.sorted()
                    .toList()
            jsonFiles.forEach { jsonFile ->
                val details = getTvTJsonData(jsonFile.toString())

                if (details != null) {
                    val htmlFileName = jsonFile.name.replace(".json", ".html")

                    val htmlFullName = "$fqnHtmlName/$htmlFileName"
                    val file = File(htmlFullName)

                    recordPage.generateTeamVsTeamRecordsPage(
                        details,
                        file
                    )
                }

            }


        }

        private fun generateHtmlIndexAndTeamPagesForAllCompetitions(
            jsonDirectoryBaseName: String,
            jsonDirectoryName: String,
            htmlOutputDirectoryName: String,
        ) {
            val jsonDirectory = Paths.get(jsonDirectoryName)

            val streamOfSubdirectories = Files.list(jsonDirectory)

            val matchSubtypeDirectories =
                streamOfSubdirectories.filter { it.isDirectory() }.sorted().toList()

            streamOfSubdirectories.close()

            matchSubtypeDirectories.forEach {
                generateHtmlIndexAndTeamPagesForAllCompetitions(
                    jsonDirectoryBaseName,
                    it.toString(),
                    htmlOutputDirectoryName
                )
            }

            val recordPage = GenerateHtml()

            val fqn = jsonDirectory.toString()
            val fqnHtmlName = fqn.replace(jsonDirectoryBaseName, htmlOutputDirectoryName)

            val jsonFiles =
                Files.list(jsonDirectory)
                    .filter { it.isRegularFile() }
                    .filter { it.name == "index.json" }
                    .sorted()
                    .toList()

            if (jsonFiles.size > 0) {
                val jsonFile = jsonFiles.first()
                val details = getIndexPageJsonData(jsonFile.toString())

                if (details != null) {
                    val htmlFileName = jsonFile.name.replace(".json", ".html")

                    val htmlFullName = "$fqnHtmlName/$htmlFileName"

                    recordPage.generateIndexPageForTeamsAndType(
                        details.teamNames.sorted(),
                        details.matchSubType,
                        details.country,
                        details.gender,
                        details.title,
                        details.extraMessages,
                        htmlFullName
                    )
                }

            }
        }

        private fun generateHtmlTeamPairHomePagesForAllCompetitions(
            jsonDirectoryBaseName: String,
            jsonDirectoryName: String,
            htmlOutputDirectoryName: String
        ) {
            val jsonDirectory = Paths.get(jsonDirectoryName)

            val streamOfSubdirectories = Files.list(jsonDirectory)


            val matchSubtypeDirectories =
                streamOfSubdirectories.filter { it.isDirectory() }.sorted().toList()

            streamOfSubdirectories.close()

            matchSubtypeDirectories.forEach { path ->
                generateHtmlTeamPairHomePagesForAllCompetitions(
                    jsonDirectoryBaseName,
                    path.toString(),
                    htmlOutputDirectoryName
                )
            }

            val recordPage = GenerateHtml()

            val fqn = jsonDirectory.toString()
            val fqnHtmlName = fqn.replace(jsonDirectoryBaseName, htmlOutputDirectoryName)

            val jsonFiles =
                Files.list(jsonDirectory)
                    .filter { it.isRegularFile() }
                    .filter { !it.name.contains("_v_") }
                    .filter { it.name != "index.json" }
                    .sorted()
                    .toList()
            jsonFiles.forEach { jsonFile ->
                val details = getHomePageJsonData(jsonFile.toString())

                if (details != null) {
                    val htmlFileName = jsonFile.name.replace(".json", ".html")

                    val htmlFullName = "$fqnHtmlName/$htmlFileName"
                    val file = File(htmlFullName)

                    recordPage.createTeamPairHomePages(details, file)

                }
            }

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





