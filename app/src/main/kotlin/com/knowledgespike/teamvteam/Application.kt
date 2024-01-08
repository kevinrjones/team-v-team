package com.knowledgespike.teamvteam

import com.knowledgespike.db.tables.references.TEAMS
import com.knowledgespike.db.tables.references.TEAMSMATCHTYPES
import com.knowledgespike.teamvteam.data.Competition
import com.knowledgespike.teamvteam.data.OpponentWithAuthors
import com.knowledgespike.teamvteam.data.Team
import com.knowledgespike.teamvteam.database.ProcessTeams
import com.knowledgespike.teamvteam.database.TeamPairDetails
import com.knowledgespike.teamvteam.html.GenerateHtml
import com.knowledgespike.teamvteam.json.TeamPairDetailsData
import com.knowledgespike.teamvteam.json.writeJson
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.apache.commons.cli.*
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import kotlin.io.path.isRegularFile
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

                val jsonOutputDirectory = "$baseDirectory/$relativeJsonOutputDirectory"
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

            val allCompetitions = mutableListOf<Competition>()
            withContext(Dispatchers.IO) {
                Files.list(Paths.get(baseDirectory))
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

                val jobs = mutableListOf<Job>()

                allCompetitions.forEach { competition: Competition ->
                    val job = launch {

                        val gender = competition.gender
                        val matchDesignator = competition.title
                        val matchSubType = competition.subType

                        var pairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()

                        val recordPage = GenerateHtml()

                        val country = competition.country

                        val sortedTeamNames: List<String> = competition.teams.map { it.team }.sorted()


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

                        // todo: map of map, do both maps have the same key?
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

                        val outputDirectory = "${htmlOutputDirectory}/${competition.outputDirectory}"
                        recordPage.generateIndexPageForTeamsAndType(
                            sortedTeamNames,
                            matchSubType,
                            country,
                            gender,
                            matchDesignator,
                            outputDirectory,
                            competition.extraMessages
                        )

                        processTeams.process(
                            connectionString,
                            userName,
                            password,
                            matchSubType
                        ) { teamPairDetails ->

                            log.debug(
                                "Updating data for {} and {} for {}",
                                teamPairDetails.teams[0],
                                teamPairDetails.teams[1],
                                matchDesignator
                            )

                            val teamPairDetailsData = TeamPairDetailsData(
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
                                teamPairDetails.worstBowlingERInnings,
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


                            val fullJsonOutputDirectory = "$jsonOutputDirectory/${competition.outputDirectory}/${matchSubType}"
                            val fileName = "${teamPairDetails.teams[0].replace(" ", "_")}_v_${
                                teamPairDetails.teams[1].replace(" ", "_")
                            }.json"

                            writeJson(fullJsonOutputDirectory, fileName, teamPairDetailsData)
//                            pairsForPage = addPairToCollectionForPairVPairPages(
//                                competition.teams.map { it.team },
//                                teamPairDetails,
//                                pairsForPage
//                            )
//
//                            recordPage.generateTeamVsTeamRecordsPage(
//                                teamPairDetails,
//                                matchDesignator,
//                                matchSubType,
//                                outputDirectory
//                            )
                        }

//                        recordPage.createTeamPairHomePages(
//                            matchSubType,
//                            matchDesignator,
//                            pairsForPage,
//                            country,
//                            gender,
//                            outputDirectory
//                        )
                    }
                    jobs.add(job)
                }
                jobs.forEach { j -> j.join() }
                log.info("processAllDirectories finished")

            }
        }

        private fun addPairToCollectionForPairVPairPages(
            competitionTeams: List<String>,
            teamPair: TeamPairDetails,
            pairsForPage: Map<String, TeamPairHomePagesData>
        ): MutableMap<String, TeamPairHomePagesData> {
            val mutablePairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()
            mutablePairsForPage.putAll(pairsForPage)
            var teamName = teamPair.teams[0]
            var hasOwnPage = competitionTeams.contains(teamName)

            var pairEx = pairsForPage[teamName]
            if (pairEx == null) {
                val teamPairList = mutableListOf<TeamPairDetails>()
                teamPairList.add(teamPair)
                mutablePairsForPage[teamPair.teams[0]] = TeamPairHomePagesData(hasOwnPage, teamPairList)
            } else {
                pairEx.teamPairDetails.add(teamPair)
            }
            teamName = teamPair.teams[1]
            pairEx = pairsForPage[teamName]
            if (pairEx == null) {
                hasOwnPage = competitionTeams.contains(teamName)
                val teamPairList = mutableListOf<TeamPairDetails>()
                teamPairList.add(teamPair)
                mutablePairsForPage[teamPair.teams[1]] = TeamPairHomePagesData(hasOwnPage, teamPairList)
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

