package com.knowledgespike.teamvteam

import com.knowledgespike.db.tables.references.TEAMS
import com.knowledgespike.db.tables.references.TEAMSMATCHTYPES
import com.knowledgespike.teamvteam.data.Competition
import com.knowledgespike.teamvteam.data.Team
import com.knowledgespike.teamvteam.database.ProcessTeams
import com.knowledgespike.teamvteam.database.TeamPairDetails
import com.knowledgespike.teamvteam.html.GenerateHtml
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import kotlinx.coroutines.*
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

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {

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
            val outputDirectory = cmd.getOptionValue("o")

            processAllCompetitions(baseDirectory, outputDirectory, connectionString, userName, password)
        }

        private suspend fun processAllCompetitions(
            baseDirectory: String,
            baseOutputDirectory: String,
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
                        val matchType = competition.type

                        val pairsForPage: MutableMap<String, TeamPairHomePagesData> = mutableMapOf()

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
                                matchType
                            )

                        val opponentsForTeam = mutableMapOf<String, TeamNameToIds>()
                        competition.teams.forEach {
                            val id = teamsWithDuplicates.get(it.team)
                            val opponents: TeamNameToIds =
                                getTeamIdsForTeamNames(connectionString, userName, password, it.opponents, matchType)
                            opponentsForTeam.put(it.team, opponents)
                        }

                        val processTeams = ProcessTeams(teamsWithDuplicates, opponentsForTeam)

                        val outputDirectory = "${baseOutputDirectory}/${competition.outputDirectory}"
                        recordPage.generateIndexPageForTeamsAndType(
                            sortedTeamNames,
                            matchType,
                            country,
                            gender,
                            matchDesignator,
                            outputDirectory,
                            competition.extraMessages
                        )

                        processTeams(connectionString, userName, password, matchType) { hasOwnPage, teamPairDetails ->

                            addPairToCollectionForPairVPairPages(
                                hasOwnPage,
                                pairsForPage,
                                teamPairDetails
                            )

                            recordPage.generateTeamVsTeamRecordsPage(
                                teamPairDetails,
                                matchDesignator,
                                matchType,
                                country,
                                outputDirectory
                            )
                        }

                        recordPage.createTeamPairHomePages(
                            matchType,
                            matchDesignator,
                            pairsForPage,
                            country,
                            gender,
                            outputDirectory
                        )
                    }
                    jobs.add(job)
                }
                jobs.forEach { j -> j.join() }
                log.info("processAllDirectories finished")

            }
        }

        private fun addPairToCollectionForPairVPairPages(
            hasOwnPage: Boolean,
            pairsForPage: MutableMap<String, TeamPairHomePagesData>,
            teamPair: TeamPairDetails
        ) {
            var pairEx = pairsForPage[teamPair.teamA]
            if (pairEx == null) {
                val teamPairList = mutableListOf<TeamPairDetails>()
                teamPairList.add(teamPair)
                pairsForPage[teamPair.teamA] = TeamPairHomePagesData(hasOwnPage, teamPairList)
            } else {
                pairEx.teamPairDetails.add(teamPair)
            }
            pairEx = pairsForPage[teamPair.teamB]
            if (pairEx == null) {
                val teamPairList = mutableListOf<TeamPairDetails>()
                teamPairList.add(teamPair)
                pairsForPage[teamPair.teamB] = TeamPairHomePagesData(hasOwnPage, teamPairList)
            } else {
                pairEx.teamPairDetails.add(teamPair)
            }
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
                val context = DSL.using(conn, SQLDialect.MYSQL)

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
                val context = DSL.using(conn, SQLDialect.MYSQL)

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

            val outputLocationOption = Option
                .builder("o")
                .hasArg()
                .desc("the output directory")
                .argName("output directory name")
                .longOpt("outputDirectory")
                .required()
                .build()


            options.addOption(connectionStringOption)
            options.addOption(userStringOption)
            options.addOption(passwordOption)
            options.addOption(baseDirectoryOption)
            options.addOption(outputLocationOption)

            return options
        }
    }

}

