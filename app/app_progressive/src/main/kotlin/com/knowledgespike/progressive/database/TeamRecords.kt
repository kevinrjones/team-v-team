package com.knowledgespike.progressive.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.progressive.data.BestBowlingDto
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.DatabaseConnection
import org.jooq.DSLContext
import org.jooq.WithStep
import org.jooq.impl.DSL.*
import java.sql.DriverManager
import java.time.format.DateTimeFormatter


class TeamRecords(private val databaseConnection: DatabaseConnection) {

    private var inputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var outputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val VS_ALL_SCORE_LIMIT = 75
    private val VS_ALL_WICKET_LIMIT = 4

    fun getHighestTotals(
        teamParams: TeamParams,
    ): List<TotalDto> {
        val highestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.SYNTHETICTOTAL.`as`("synth"),
//                        INNINGS.TOTAL.add((INNINGS.WICKETS.cast(Float::class.java).div(10))).`as`("synth"),
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.OPPONENTSID
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(
                            INNINGS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        )
                        .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                        .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("synth"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("OpponentsId"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.opponentsId", Int::class.java)))
                .where(field("synth").ge(field("premax")))
                .orderBy(
                    field("synth"),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.team,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestTotals.add(hs)
            }
        }

        return highestTotals
    }


    fun getLowestAllOutTotals(
        teamParams: TeamParams,
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.TEAMID
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(INNINGS.COMPLETE.eq(1))
                        .and(
                            INNINGS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        )
                        .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                        .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("location"),
                        field("teamid"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        min(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.teamid", Int::class.java)))
                .where(field("total").le(field("premax")))
                .orderBy(
                    field("total").desc(),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.team,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                lowestTotals.add(hs)
            }
        }

        return lowestTotals
    }

    fun getHighestIndividualScores(teamParams: TeamParams): List<HighestScoreDto> {
        val highestscores = mutableListOf<HighestScoreDto>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                        BATTINGDETAILS.SCORE,
                        BATTINGDETAILS.NOTOUT,
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                        BATTINGDETAILS.INNINGSORDER,
                        BATTINGDETAILS.POSITION,
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.MATCHSTARTDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                    )
                        .from(BATTINGDETAILS)
                        .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .and(BATTINGDETAILS.SCORE.isNotNull)
                        .and(BATTINGDETAILS.SCORE.ge(25))
                        .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                        .and(
                            BATTINGDETAILS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        )
                        .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .orderBy(
                            BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                            BATTINGDETAILS.INNINGSORDER,
                            BATTINGDETAILS.POSITION
                        )
                ).with("cte1").`as`(
                    select(
                        field("fullname"),
                        field("sortnamepart"),
                        field("score"),
                        field("notout"),
                        field("notoutadjustedscore"),
                        field("inningsorder"),
                        field("position"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("notoutadjustedscore")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("position")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

                .select(
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("score", Int::class.java),
                    field("notout", Boolean::class.java),
                    field("location", String::class.java),
                    field("matchstartdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte1")

                .where(field("notoutadjustedscore").ge(field("premax")))
                .orderBy(field("score"))
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                // want only one but there may be multiple scores with the same value
                val hs = HighestScoreDto(
                    r.getValue("fullname", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("score", Int::class.java),
                    r.getValue("notout", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestscores.add(hs)
            }

        }

        return highestscores
    }

    fun getBestBowlingInnings(teamParams: TeamParams): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val cte = context
                .with("cte").`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.RUNS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID)
                                .and(PLAYERSMATCHES.MATCHID.eq(BOWLINGDETAILS.MATCHID))
                        )
                        .where(BOWLINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                        .and(
                            BOWLINGDETAILS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        )
                        .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .and(BOWLINGDETAILS.WICKETS.ge(3))
                ).with("cte1").`as`(
                    select(
                        field("fullname"),
                        field("sortnamepart"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("runs"),
                        field("wickets"),
                        field("inningsorder"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("syntheticbestbowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                )

            val results = cte.select(
                field("wickets", Int::class.java),
                field("runs", Int::class.java),
                field("balls", Int::class.java),
                field("maidens", Int::class.java),
                field("ballsperover", Int::class.java),
                field("SyntheticBestBowling", Double::class.java),
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte1")
                .where(field("syntheticbestbowling").ge(field("premax")))
                .fetch()

            for (row in results) {
                val date = row.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val bb = BestBowlingDto(
                    row.getValue("fullname", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("ballsperover", Int::class.java),
                    row.getValue("balls", Int::class.java),
                    row.getValue("maidens", Int::class.java),
                    row.getValue("wickets", Int::class.java),
                    row.getValue("runs", Int::class.java),
                    row.getValue("location").toString(),
                    matchDate
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getBestBowlingInningsForSelectedTeamVsAllTeams(teamParams: TeamParams, overall: Boolean): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        var whereClause = (BOWLINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BOWLINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BOWLINGDETAILS.WICKETS.ge(VS_ALL_WICKET_LIMIT))
            .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))

        if (!overall)
            whereClause = whereClause.and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))


        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val cte = context
                .with("cte").`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.RUNS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BOWLINGDETAILS.OPPONENTSID
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID)
                                .and(PLAYERSMATCHES.MATCHID.eq(BOWLINGDETAILS.MATCHID))
                        )
                        .where(whereClause)
                ).with("cte1").`as`(
                    select(
                        field("opponentsid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("runs"),
                        field("wickets"),
                        field("inningsorder"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("syntheticbestbowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                )

            val results = cte.select(
                field("wickets", Int::class.java),
                field("runs", Int::class.java),
                field("balls", Int::class.java),
                field("maidens", Int::class.java),
                field("ballsperover", Int::class.java),
                field("SyntheticBestBowling", Double::class.java),
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
                field("name"),
            ).from("cte1")
                .join(TEAMS).on(TEAMS.ID.eq(field("opponentsid", Int::class.java)))
                .where(field("syntheticbestbowling").ge(field("premax")))
                .orderBy(field("SyntheticBestBowling"), field("matchstartdateasoffset"))
                .fetch()

            for (row in results) {
                val date = row.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val bb = BestBowlingDto(
                    row.getValue("fullname", String::class.java),
                    teamParams.team,
                    row.getValue("name", String::class.java),
                    row.getValue("ballsperover", Int::class.java),
                    row.getValue("balls", Int::class.java),
                    row.getValue("maidens", Int::class.java),
                    row.getValue("wickets", Int::class.java),
                    row.getValue("runs", Int::class.java),
                    row.getValue("location").toString(),
                    matchDate
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getBestBowlingInningsForAllTeamsVsSelectedTeam(teamParams: TeamParams, overall: Boolean): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        var whereClause = (BOWLINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BOWLINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BOWLINGDETAILS.WICKETS.ge(VS_ALL_WICKET_LIMIT))
            .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

        if (!overall)
            whereClause = whereClause.and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val cte = context
                .with("cte").`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.RUNS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BOWLINGDETAILS.TEAMID
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID)
                                .and(PLAYERSMATCHES.MATCHID.eq(BOWLINGDETAILS.MATCHID))
                        )
                        .where(whereClause)
                ).with("cte1").`as`(
                    select(
                        field("teamid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("runs"),
                        field("wickets"),
                        field("inningsorder"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("syntheticbestbowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                )

            val results = cte.select(
                field("wickets", Int::class.java),
                field("runs", Int::class.java),
                field("balls", Int::class.java),
                field("maidens", Int::class.java),
                field("ballsperover", Int::class.java),
                field("SyntheticBestBowling", Double::class.java),
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
                field("name"),
            ).from("cte1")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte1.teamid", Int::class.java)))
                .where(field("syntheticbestbowling").ge(field("premax")))
                .orderBy(field("SyntheticBestBowling"), field("matchstartdateasoffset"))
                .fetch()

            for (row in results) {
                val date = row.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val bb = BestBowlingDto(
                    row.getValue("fullname", String::class.java),
                    teamParams.opponents,
                    row.getValue("name", String::class.java),
                    row.getValue("ballsperover", Int::class.java),
                    row.getValue("balls", Int::class.java),
                    row.getValue("maidens", Int::class.java),
                    row.getValue("wickets", Int::class.java),
                    row.getValue("runs", Int::class.java),
                    row.getValue("location").toString(),
                    matchDate
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getBestBowlingMatch(teamParams: TeamParams): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        val t = TEAMS.`as`("t")
        val o = TEAMS.`as`("o")

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            try {
                val context = using(conn, databaseConnection.dialect)

                val q = context.with(
                    "cte"
                ).`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.NAME,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        rowNumber().over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                            BOWLINGDETAILS.PLAYERID
                        ).`as`("rn"),
                        coalesce(
                            sum(BOWLINGDETAILS.BALLS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("balls"),
                        coalesce(
                            sum(BOWLINGDETAILS.MAIDENS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("maidens"),
                        coalesce(
                            sum(BOWLINGDETAILS.WICKETS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("wickets"),
                        coalesce(
                            sum(BOWLINGDETAILS.RUNS).over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("runs"),
                        coalesce(
                            sum(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("SyntheticBestBowling"),
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID).and(
                                PLAYERSMATCHES.MATCHID.eq(
                                    BOWLINGDETAILS.MATCHID
                                )
                            )
                        )
                        .join(t).on(t.ID.eq(BOWLINGDETAILS.TEAMID))
                        .join(o).on(o.ID.eq(BOWLINGDETAILS.OPPONENTSID))
                        .where(BOWLINGDETAILS.MATCHTYPE.eq(teamParams.matchType))
                        .and(
                            BOWLINGDETAILS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        )
                        .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                ).with("cte2").`as`(
                    select(
                        field("fullname"),
                        field("sortnamepart"),
                        field("seriesdate"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("location"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("wickets"),
                        field("runs"),
                        field("SyntheticBestBowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                        .where(field("SyntheticBestBowling").isNotNull).and(field("rn").eq(1))
                )

                val query = q.select()
                    .from("cte2")
                    .where(field("SyntheticBestBowling").ge(field("premax")))
                    .orderBy(field("SyntheticBestBowling"), field("matchstartdateasoffset"))

                val result = query.fetch()

                var previous = 0.0
                for (row in result) {

                    val current = row.getValue("SyntheticBestBowling", Double::class.java)
                    // want only one but there may be multiple scores with the same value
                    if (previous <= current) {
                        val date = row.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)


                        val bb = BestBowlingDto(
                            row.getValue("fullname", String::class.java),
                            teamParams.team,
                            teamParams.opponents,
                            row.getValue("ballsperover", Int::class.java),
                            row.getValue("balls", Int::class.java),
                            row.getValue("maidens", Int::class.java),
                            row.getValue("wickets", Int::class.java),
                            row.getValue("runs", Int::class.java),
                            row.getValue("location").toString(),
                            matchDate
                        )
                        bestBowling.add(bb)
                    } else {
                        break
                    }
                    previous = current
                }
            } catch (e: Exception) {
                println("")
                throw e
            }
        }
        return bestBowling
    }

    fun getProgressivePartnershipRecords(teamParams: TeamParams, overall: Boolean): Map<Int, FowDetails> {

        val partnershipLimit = 0
        val matchStartDateAsOffset = -9999999999L

        val bestFow = mutableMapOf<Int, FowDetails>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val tmpTableName = "tmp_partnerships"
            createTemporaryFoWTable(context, tmpTableName, teamParams, overall)

            try {
                for (wicket in 1..10) {
                    val listFoW = mutableListOf<FoWDto>()
                    val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()

                    val query =
                        createPartialFoWResult(context, tmpTableName, wicket, partnershipLimit, matchStartDateAsOffset)

                    val result = query.select(
                        field("teamid"),
                        field("opponentsid"),
                        field("wicket"),
                        field("inningsorder"),
                        field("partnership"),
                        field("unbroken"),
                        field("playernames"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("fullname"),
                        field("score"),
                        field("notout"),
                        field("position"),
                        field("fullname2"),
                        field("score2"),
                        field("notout2"),
                        field("position2"),
//                        field("Name"),
                    )
                        .from("cte2")
                        .where(field("partnership").ge(field("premax")))
                        .orderBy(
                            field("partnership"),
                            field("matchstartdateasoffset"),
                            field("inningsorder")
                        )
                        .fetch()

                    for (partnershipRecord in result) {

                        val date = partnershipRecord.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)

                        // want only one but there may be multiple scores with the same value
                        val fow = FoWDto(
                            teamParams.team,
                            teamParams.opponents,
                            partnershipRecord.getValue("location", String::class.java),
                            matchDate,
                            partnershipRecord.getValue("partnership", Int::class.java),
                            partnershipRecord.getValue("wicket", Int::class.java),
                            partnershipRecord.getValue("unbroken", Boolean::class.java),
                            partnershipRecord.getValue("fullname", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score", Int::class.java),
                            partnershipRecord.getValue("notout", Boolean::class.java),
                            partnershipRecord.getValue("position", Int::class.java),
                            partnershipRecord.getValue("fullname2", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score2", Int::class.java),
                            partnershipRecord.getValue("notout2", Boolean::class.java),
                            partnershipRecord.getValue("position2", Int::class.java),
                        )


                        listFoW.add(fow)
                    }
                    bestFow[wicket] = FowDetails(listFoW, listMultiPlayerFowDao)

                }
            } finally {
                context.dropTable(tmpTableName)
            }
        }
        return bestFow
    }

    fun getProgressivePartnershipRecordsForSelectedTeamVsAllTeams(
        teamParams: TeamParams,
        overall: Boolean,
    ): Map<Int, FowDetails> {

        val partnershipLimit = 0
        val matchStartDateAsOffset = -9999999999L

        val bestFow = mutableMapOf<Int, FowDetails>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val tmpTableName = "tmp_partnerships"
            createTemporaryFoWTableAllTeams(context, tmpTableName, teamParams, overall, true)

            try {
                for (wicket in 1..10) {
                    val listFoW = mutableListOf<FoWDto>()
                    val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()

                    val query =
                        createPartialFoWResult(context, tmpTableName, wicket, partnershipLimit, matchStartDateAsOffset)

                    val result = query.select(
                        field("cte2.teamid"),
                        field("opponentsid"),
                        field("wicket"),
                        field("inningsorder"),
                        field("partnership"),
                        field("unbroken"),
                        field("playernames"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("fullname"),
                        field("score"),
                        field("notout"),
                        field("position"),
                        field("fullname2"),
                        field("score2"),
                        field("notout2"),
                        field("position2"),
                        field("name"),
                    )
                        .from("cte2")
                        .join(TEAMS).on(TEAMS.ID.eq(field("cte2.OpponentsId", Int::class.java)))
                        .where(field("partnership").ge(field("premax")))
                        .orderBy(
                            field("partnership"),
                            field("matchstartdateasoffset"),
                            field("inningsorder")
                        )
                        .fetch()

                    for (partnershipRecord in result) {

                        val date = partnershipRecord.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)

                        // want only one but there may be multiple scores with the same value
                        val fow = FoWDto(
                            teamParams.team,
                            partnershipRecord.getValue("name", String::class.java),
                            partnershipRecord.getValue("location", String::class.java),
                            matchDate,
                            partnershipRecord.getValue("partnership", Int::class.java),
                            partnershipRecord.getValue("wicket", Int::class.java),
                            partnershipRecord.getValue("unbroken", Boolean::class.java),
                            partnershipRecord.getValue("fullname", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score", Int::class.java),
                            partnershipRecord.getValue("notout", Boolean::class.java),
                            partnershipRecord.getValue("position", Int::class.java),
                            partnershipRecord.getValue("fullname2", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score2", Int::class.java),
                            partnershipRecord.getValue("notout2", Boolean::class.java),
                            partnershipRecord.getValue("position2", Int::class.java),
                        )


                        listFoW.add(fow)
                    }
                    bestFow[wicket] = FowDetails(listFoW, listMultiPlayerFowDao)

                }
            } finally {
                context.dropTable(tmpTableName)
            }
        }
        return bestFow
    }

    fun getProgressivePartnershipRecordsForAllTeamsVsSelectedTeam(
        teamParams: TeamParams,
        overall: Boolean,
    ): Map<Int, FowDetails> {

        val partnershipLimit = 0
        val matchStartDateAsOffset = -9999999999L

        val bestFow = mutableMapOf<Int, FowDetails>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            val tmpTableName = "tmp_partnerships"
            createTemporaryFoWTableAllTeams(context, tmpTableName, teamParams, overall, false)

            try {
                for (wicket in 1..10) {
                    val listFoW = mutableListOf<FoWDto>()
                    val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()

                    val query =
                        createPartialFoWResult(context, tmpTableName, wicket, partnershipLimit, matchStartDateAsOffset)

                    val result = query.select(
                        field("cte2.teamid"),
                        field("opponentsid"),
                        field("wicket"),
                        field("inningsorder"),
                        field("partnership"),
                        field("unbroken"),
                        field("playernames"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("fullname"),
                        field("score"),
                        field("notout"),
                        field("position"),
                        field("fullname2"),
                        field("score2"),
                        field("notout2"),
                        field("position2"),
                        field("name"),
                    )
                        .from("cte2")
                        .join(TEAMS).on(TEAMS.ID.eq(field("cte2.TeamId", Int::class.java)))
                        .where(field("partnership").ge(field("premax")))
                        .orderBy(
                            field("partnership"),
                            field("matchstartdateasoffset"),
                            field("inningsorder")
                        )
                        .fetch()

                    for (partnershipRecord in result) {

                        val date = partnershipRecord.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)

                        // want only one but there may be multiple scores with the same value
                        val fow = FoWDto(
                            teamParams.opponents,
                            partnershipRecord.getValue("name", String::class.java),
                            partnershipRecord.getValue("location", String::class.java),
                            matchDate,
                            partnershipRecord.getValue("partnership", Int::class.java),
                            partnershipRecord.getValue("wicket", Int::class.java),
                            partnershipRecord.getValue("unbroken", Boolean::class.java),
                            partnershipRecord.getValue("fullname", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score", Int::class.java),
                            partnershipRecord.getValue("notout", Boolean::class.java),
                            partnershipRecord.getValue("position", Int::class.java),
                            partnershipRecord.getValue("fullname2", String::class.java) ?: "unknown",
                            partnershipRecord.getValue("score2", Int::class.java),
                            partnershipRecord.getValue("notout2", Boolean::class.java),
                            partnershipRecord.getValue("position2", Int::class.java),
                        )


                        listFoW.add(fow)
                    }
                    bestFow[wicket] = FowDetails(listFoW, listMultiPlayerFowDao)

                }
            } finally {
                context.dropTable(tmpTableName)
            }
        }
        return bestFow
    }

    private fun createPartialFoWResult(
        context: DSLContext,
        tmpTableName: String,
        wicket: Int,
        partnershipLimit: Int,
        matchStartDateAsOffset: Long,
    ): WithStep {
        val query = context
            .with("cte").`as`(
                select(
                    field("teamid"),
                    field("opponentsid"),
                    field("wicket"),
                    field("inningsorder"),
                    field("partnership"),
                    field("unbroken"),
                    field("playernames"),
                    field("location"),
                    field("matchstartdate"),
                    field("matchstartdateasoffset"),
                    field("fullname"),
                    field("score"),
                    field("notout"),
                    field("position"),
                    field("fullname2"),
                    field("score2"),
                    field("notout2"),
                    field("position2")
                )
                    .from(tmpTableName)
                    .where(field("wicket").eq(wicket))
                    .and(field("rn").eq(1))
                    .and(field("partnership").ge(partnershipLimit))
                    .and(field("matchstartdateasoffset").ge(matchStartDateAsOffset))
                    .orderBy(field("matchstartdateasoffset"), field("inningsorder"))
            ).with("cte2").`as`(
                select(
                    field("teamid"),
                    field("opponentsid"),
                    field("wicket"),
                    field("inningsorder"),
                    field("partnership"),
                    field("unbroken"),
                    field("playernames"),
                    field("location"),
                    field("matchstartdate"),
                    field("matchstartdateasoffset"),
                    field("fullname"),
                    field("score"),
                    field("notout"),
                    field("position"),
                    field("fullname2"),
                    field("score2"),
                    field("notout2"),
                    field("position2"),
                    max(field("partnership")).over()
                        .orderBy(
                            field("matchstartdateasoffset"),
                            field("inningsorder")
                        )
                        .rowsBetweenUnboundedPreceding().andCurrentRow()
                        .`as`("premax")
                )
                    .from("cte")
            )
        return query
    }

    private fun createTemporaryFoWTableAllTeams(
        context: DSLContext,
        tmpTableName: String,
        teamParams: TeamParams,
        overall: Boolean,
        vsAll: Boolean,
    ) {

        var whereClause = (PARTNERSHIPS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                PARTNERSHIPS.matches.ID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(PARTNERSHIPS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(PARTNERSHIPS.MULTIPLE.eq(0))

        if (vsAll) {
            whereClause = whereClause.and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            if (!overall)
                whereClause = whereClause.and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
        } else {
            whereClause = whereClause.and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            if (!overall)
                whereClause = whereClause.and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
        }

        context.createTemporaryTable(tmpTableName).`as`(
            select(
                PARTNERSHIPS.matches.ID.`as`("matchid"),
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.OPPONENTSID,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.PARTNERSHIP,
                PARTNERSHIPS.UNBROKEN,
                PARTNERSHIPS.PLAYERNAMES,
                PARTNERSHIPS.matches.LOCATION,
                PARTNERSHIPS.matches.MATCHSTARTDATE,
                PARTNERSHIPS.matches.MATCHSTARTDATEASOFFSET,
                PLAYERSMATCHES.FULLNAME,
                BATTINGDETAILS.SCORE,
                BATTINGDETAILS.NOTOUT,
                BATTINGDETAILS.POSITION,
                lead(PLAYERSMATCHES.FULLNAME).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("fullname2"),
                lead(BATTINGDETAILS.SCORE).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("score2"),
                lead(BATTINGDETAILS.NOTOUT).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("notout2"),
                lead(BATTINGDETAILS.POSITION).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("position2"),
                rowNumber().over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("rn"),
            )
                .from(PARTNERSHIPS)
                .join(MATCHES).on(PARTNERSHIPS.MATCHID.eq(MATCHES.ID))
                .leftOuterJoin(PARTNERSHIPSPLAYERS).on(PARTNERSHIPSPLAYERS.PARTNERSHIPID.eq(PARTNERSHIPS.ID))
                .leftOuterJoin(PLAYERSMATCHES).on(
                    PLAYERSMATCHES.PLAYERID.eq(PARTNERSHIPSPLAYERS.PLAYERID).and(
                        PLAYERSMATCHES.MATCHID.eq(PARTNERSHIPS.matches.ID).and(PLAYERSMATCHES.PLAYERID.ne(1))
                    )
                )
                .leftOuterJoin(BATTINGDETAILS).on(
                    BATTINGDETAILS.MATCHID.eq(PARTNERSHIPS.matches.ID).and(
                        BATTINGDETAILS.PLAYERID.eq(
                            PARTNERSHIPSPLAYERS.PLAYERID
                        ).and(BATTINGDETAILS.INNINGSORDER.eq(PARTNERSHIPS.INNINGSORDER))
                    )
                )
                .where(whereClause)
                .orderBy(
                    PARTNERSHIPS.WICKET,
                    PARTNERSHIPS.PARTNERSHIP.desc(),
                    PARTNERSHIPS.UNBROKEN.desc(),
                    PARTNERSHIPS.matches.ID,
                    field("fullname2").desc(),
                    PLAYERSMATCHES.FULLNAME.desc(),
                )
        ).withData().execute()
    }

    private fun createTemporaryFoWTable(
        context: DSLContext,
        tmpTableName: String,
        teamParams: TeamParams,
        overall: Boolean,
    ) {

        var whereClause = (PARTNERSHIPS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                PARTNERSHIPS.matches.ID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(PARTNERSHIPS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(PARTNERSHIPS.MULTIPLE.eq(0))
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))

        if (!(overall && teamParams.opponents.lowercase() == "all"))
            whereClause = whereClause.and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))

        context.createTemporaryTable(tmpTableName).`as`(
            select(
                PARTNERSHIPS.matches.ID.`as`("matchid"),
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.OPPONENTSID,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.PARTNERSHIP,
                PARTNERSHIPS.UNBROKEN,
                PARTNERSHIPS.PLAYERNAMES,
                PARTNERSHIPS.matches.LOCATION,
                PARTNERSHIPS.matches.MATCHSTARTDATE,
                PARTNERSHIPS.matches.MATCHSTARTDATEASOFFSET,
                PLAYERSMATCHES.FULLNAME,
                BATTINGDETAILS.SCORE,
                BATTINGDETAILS.NOTOUT,
                BATTINGDETAILS.POSITION,
                lead(PLAYERSMATCHES.FULLNAME).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("fullname2"),
                lead(BATTINGDETAILS.SCORE).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("score2"),
                lead(BATTINGDETAILS.NOTOUT).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("notout2"),
                lead(BATTINGDETAILS.POSITION).over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("position2"),
                rowNumber().over()
                    .partitionBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    ).orderBy(
                        PARTNERSHIPS.matches.ID,
                        PARTNERSHIPS.TEAMID,
                        PARTNERSHIPS.INNINGSORDER,
                        PARTNERSHIPS.WICKET,
                        PARTNERSHIPS.PLAYERIDS
                    )
                    .`as`("rn"),
            )
                .from(PARTNERSHIPS)
                .join(MATCHES).on(PARTNERSHIPS.MATCHID.eq(MATCHES.ID))
                .leftOuterJoin(PARTNERSHIPSPLAYERS).on(PARTNERSHIPSPLAYERS.PARTNERSHIPID.eq(PARTNERSHIPS.ID))
                .leftOuterJoin(PLAYERSMATCHES).on(
                    PLAYERSMATCHES.PLAYERID.eq(PARTNERSHIPSPLAYERS.PLAYERID).and(
                        PLAYERSMATCHES.MATCHID.eq(PARTNERSHIPS.matches.ID).and(PLAYERSMATCHES.PLAYERID.ne(1))
                    )
                )
                .leftOuterJoin(BATTINGDETAILS).on(
                    BATTINGDETAILS.MATCHID.eq(PARTNERSHIPS.matches.ID).and(
                        BATTINGDETAILS.PLAYERID.eq(
                            PARTNERSHIPSPLAYERS.PLAYERID
                        ).and(BATTINGDETAILS.INNINGSORDER.eq(PARTNERSHIPS.INNINGSORDER))
                    )
                )
                .where(whereClause)
                .orderBy(
                    PARTNERSHIPS.WICKET,
                    PARTNERSHIPS.PARTNERSHIP.desc(),
                    PARTNERSHIPS.UNBROKEN.desc(),
                    PARTNERSHIPS.matches.ID,
                    field("fullname2").desc(),
                    PLAYERSMATCHES.FULLNAME.desc(),
                )
        ).withData().execute()
    }

    fun getHighestTotalsForSelectedTeamVsAllTeams(teamParams: TeamParams, overall: Boolean): List<TotalDto> {
        val highestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)

            var whereClause = (
                    INNINGS.MATCHID.`in`(
                        select(MATCHSUBTYPE.MATCHID).from(
                            MATCHSUBTYPE.where(
                                MATCHSUBTYPE.MATCHTYPE.eq(
                                    teamParams.matchSubType
                                )
                            )
                        )
                    )
                    )
                .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
                .and(INNINGS.TEAMID.`in`(teamParams.teamIds))

            if (!overall)
                whereClause = whereClause.and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))

            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.SYNTHETICTOTAL.`as`("synth"),
//                        INNINGS.TOTAL.add((INNINGS.WICKETS.cast(Float::class.java).div(10))).`as`("synth"),
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.OPPONENTSID,
                        INNINGS.TEAMID,
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("synth"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("TeamId"),
                        field("OpponentsId"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.OpponentsId", Int::class.java)))
                .where(field("synth").ge(field("premax")))
                .orderBy(
                    field("synth"),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.team,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestTotals.add(hs)
            }
        }

        return highestTotals
    }

    fun getHighestTotalsForAllTeamsVsSelectedTeam(teamParams: TeamParams, overall: Boolean): List<TotalDto> {
        val highestTotals = mutableListOf<TotalDto>()

        var whereClause = (
                INNINGS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
                )
            .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
            .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))

        if (!overall)
            whereClause = whereClause.and(INNINGS.TEAMID.`in`(teamParams.teamIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.SYNTHETICTOTAL.`as`("synth"),
//                        INNINGS.TOTAL.add((INNINGS.WICKETS.cast(Float::class.java).div(10))).`as`("synth"),
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.TEAMID,
                        INNINGS.OPPONENTSID,
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("synth"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("TeamId"),
                        field("OpponentsId"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.TeamId", Int::class.java)))
                .where(field("synth").ge(field("premax")))
                .orderBy(
                    field("synth"),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.opponents,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestTotals.add(hs)
            }
        }

        return highestTotals
    }

    fun getLowestAllOutTotalsForSelectedTeamVsAllTeams(teamParams: TeamParams, overall: Boolean): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        var whereClause = (INNINGS.COMPLETE.eq(1))
            .and(
                INNINGS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
            .and(INNINGS.TEAMID.`in`(teamParams.teamIds))

        if (!overall)
            whereClause = whereClause.and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.OPPONENTSID
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("location"),
                        field("opponentsid"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        min(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.opponentsid", Int::class.java)))
                .where(field("total").le(field("premax")))
                .orderBy(
                    field("total").desc(),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.team,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                lowestTotals.add(hs)
            }
        }

        return lowestTotals
    }

    fun getLowestAllOutTotalsForAllTeamsVsSelectedTeam(teamParams: TeamParams, overall: Boolean): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        var whereClause = (INNINGS.COMPLETE.eq(1))
            .and(
                INNINGS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
            .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))

        if (!overall)
            whereClause = whereClause.and(INNINGS.TEAMID.`in`(teamParams.teamIds))


        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.INNINGSORDER,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.MATCHSTARTDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                        INNINGS.TEAMID
                    )
                        .from(INNINGS)
                        .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("total"),
                        field("wickets"),
                        field("declared"),
                        field("inningsorder"),
                        field("location"),
                        field("teamid"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        min(field("total")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

            val result = cte.select(
                field("total", Int::class.java),
                field("wickets", Int::class.java),
                field("declared", Boolean::class.java),
                field("name", String::class.java),
                field("location", String::class.java),
                field("matchstartdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte2.teamid", Int::class.java)))
                .where(field("total").le(field("premax")))
                .orderBy(
                    field("total").desc(),
                    field("matchstartdateasoffset"),
                    field("inningsorder"),
                )
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                val hs = TotalDto(
                    teamParams.opponents,
                    r.getValue("name", String::class.java),
                    r.getValue("total", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("declared", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                lowestTotals.add(hs)
            }
        }

        return lowestTotals
    }

    fun getHighestIndividualScoresForSelectedTeamVsAllTeams(
        teamParams: TeamParams,
        overall: Boolean,
    ): Collection<HighestScoreDto> {
        val highestscores = mutableListOf<HighestScoreDto>()

        var whereClause = BATTINGDETAILS.SCORE.isNotNull
            .and(BATTINGDETAILS.SCORE.ge(VS_ALL_SCORE_LIMIT))
            .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BATTINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))

        if (!overall)
            whereClause = whereClause.and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                        BATTINGDETAILS.SCORE,
                        BATTINGDETAILS.NOTOUT,
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                        BATTINGDETAILS.INNINGSORDER,
                        BATTINGDETAILS.POSITION,
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.MATCHSTARTDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BATTINGDETAILS.OPPONENTSID
                    )
                        .from(BATTINGDETAILS)
                        .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                        .orderBy(
                            BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                            BATTINGDETAILS.INNINGSORDER,
                            BATTINGDETAILS.POSITION
                        )
                ).with("cte1").`as`(
                    select(
                        field("opponentsid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("score"),
                        field("notout"),
                        field("notoutadjustedscore"),
                        field("inningsorder"),
                        field("position"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("notoutadjustedscore")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("position")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

                .select(
                    field("fullname", String::class.java),
                    field("name", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("score", Int::class.java),
                    field("notout", Boolean::class.java),
                    field("location", String::class.java),
                    field("matchstartdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte1")
                .join(TEAMS).on(TEAMS.ID.eq(field("opponentsid", Int::class.java)))
                .where(field("notoutadjustedscore").ge(field("premax")))
                .orderBy(field("score"))
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                // want only one but there may be multiple scores with the same value
                val hs = HighestScoreDto(
                    r.getValue("fullname", String::class.java),
                    teamParams.team,
                    r.getValue("name", String::class.java),
                    r.getValue("score", Int::class.java),
                    r.getValue("notout", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestscores.add(hs)
            }
        }
        return highestscores
    }


    fun getHighestIndividualScoresForAllTeamsVsSelectedTeam(
        teamParams: TeamParams,
        overall: Boolean,
    ): Collection<HighestScoreDto> {
        val highestscores = mutableListOf<HighestScoreDto>()

        var whereClause = BATTINGDETAILS.SCORE.isNotNull
            .and(BATTINGDETAILS.SCORE.ge(VS_ALL_SCORE_LIMIT))
            .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BATTINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

        if (!overall)
            whereClause = whereClause.and(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            val context = using(conn, databaseConnection.dialect)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                        BATTINGDETAILS.SCORE,
                        BATTINGDETAILS.NOTOUT,
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                        BATTINGDETAILS.INNINGSORDER,
                        BATTINGDETAILS.POSITION,
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.MATCHSTARTDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BATTINGDETAILS.TEAMID
                    )
                        .from(BATTINGDETAILS)
                        .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .where(whereClause)
                        .orderBy(
                            BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                            BATTINGDETAILS.INNINGSORDER,
                            BATTINGDETAILS.POSITION
                        )
                ).with("cte1").`as`(
                    select(
                        field("teamid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("score"),
                        field("notout"),
                        field("notoutadjustedscore"),
                        field("inningsorder"),
                        field("position"),
                        field("location"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        max(field("notoutadjustedscore")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("inningsorder"),
                                field("position")
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow()
                            .`as`("premax")
                    ).from("cte")
                )

                .select(
                    field("fullname", String::class.java),
                    field("name", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("score", Int::class.java),
                    field("notout", Boolean::class.java),
                    field("location", String::class.java),
                    field("matchstartdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte1")
                .join(TEAMS).on(TEAMS.ID.eq(field("cte1.teamid", Int::class.java)))
                .where(field("notoutadjustedscore").ge(field("premax")))
                .orderBy(field("score"))
                .fetch()

            for (r in result) {
                val date = r.getValue("matchstartdate", String::class.java)
                val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                val matchDate = dateTime.format(outputFormatter)

                // want only one but there may be multiple scores with the same value
                val hs = HighestScoreDto(
                    r.getValue("fullname", String::class.java),
                    teamParams.opponents,
                    r.getValue("name", String::class.java),
                    r.getValue("score", Int::class.java),
                    r.getValue("notout", Boolean::class.java),
                    r.getValue("location").toString(),
                    matchDate
                )
                highestscores.add(hs)
            }
        }
        return highestscores
    }

    fun getBestBowlingMatchForSelectedTeamVsAllTeams(
        teamParams: TeamParams,
        overall: Boolean,
    ): Collection<BestBowlingDto> {
        val bestBowling = mutableListOf<BestBowlingDto>()

        val t = TEAMS.`as`("t")
        val o = TEAMS.`as`("o")

        var whereClause = (BOWLINGDETAILS.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BOWLINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))

        if (!overall)
            whereClause = whereClause.and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            try {
                val context = using(conn, databaseConnection.dialect)

                val q = context.with(
                    "cte"
                ).`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.NAME,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BOWLINGDETAILS.OPPONENTSID,
                        rowNumber().over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                            BOWLINGDETAILS.PLAYERID
                        ).`as`("rn"),
                        coalesce(
                            sum(BOWLINGDETAILS.BALLS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("balls"),
                        coalesce(
                            sum(BOWLINGDETAILS.MAIDENS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("maidens"),
                        coalesce(
                            sum(BOWLINGDETAILS.WICKETS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("wickets"),
                        coalesce(
                            sum(BOWLINGDETAILS.RUNS).over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("runs"),
                        coalesce(
                            sum(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("SyntheticBestBowling"),
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID).and(
                                PLAYERSMATCHES.MATCHID.eq(
                                    BOWLINGDETAILS.MATCHID
                                )
                            )
                        )
                        .join(t).on(t.ID.eq(BOWLINGDETAILS.TEAMID))
                        .join(o).on(o.ID.eq(BOWLINGDETAILS.OPPONENTSID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("opponentsid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("seriesdate"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("location"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("wickets"),
                        field("runs"),
                        field("SyntheticBestBowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                        .where(field("SyntheticBestBowling").isNotNull)
                        .and(field("rn").eq(1))
                        .and(field("cte.wickets").gt(5))
                )

                val query = q.select(
                    field("fullname"),
                    field("sortnamepart"),
                    field("seriesdate"),
                    field("matchstartdate"),
                    field("matchstartdateasoffset"),
                    field("location"),
                    field("ballsperover"),
                    field("balls"),
                    field("maidens"),
                    field("wickets"),
                    field("runs"),
                    field("SyntheticBestBowling"),
                    field("name")
                )
                    .from("cte2")
                    .join(TEAMS).on(TEAMS.ID.eq(field("opponentsid", Int::class.java)))
                    .where(field("SyntheticBestBowling").ge(field("premax")))
                    .orderBy(field("SyntheticBestBowling"), field("matchstartdateasoffset"))

                val result = query.fetch()

                var previous = 0.0
                for (row in result) {

                    val current = row.getValue("SyntheticBestBowling", Double::class.java)
                    // want only one but there may be multiple scores with the same value
                    if (previous <= current) {
                        val date = row.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)

                        val bb = BestBowlingDto(
                            row.getValue("fullname", String::class.java),
                            teamParams.team,
                            row.getValue("name", String::class.java),
                            row.getValue("ballsperover", Int::class.java),
                            row.getValue("balls", Int::class.java),
                            row.getValue("maidens", Int::class.java),
                            row.getValue("wickets", Int::class.java),
                            row.getValue("runs", Int::class.java),
                            row.getValue("location").toString(),
                            matchDate
                        )
                        bestBowling.add(bb)
                    } else {
                        break
                    }
                    previous = current
                }
            } catch (e: Exception) {
                println("")
                throw e
            }
        }
        return bestBowling
    }

    fun getBestBowlingMatchForAllTeamsVsSelectedTeam(
        teamParams: TeamParams,
        overall: Boolean,
    ): Collection<BestBowlingDto> {
        val bestBowling = mutableListOf<BestBowlingDto>()

        var whereClause = (BOWLINGDETAILS.MATCHTYPE.eq(teamParams.matchType))
            .and(
                BOWLINGDETAILS.MATCHID.`in`(
                    select(MATCHSUBTYPE.MATCHID).from(
                        MATCHSUBTYPE.where(
                            MATCHSUBTYPE.MATCHTYPE.eq(
                                teamParams.matchSubType
                            )
                        )
                    )
                )
            )
            .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
            .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

        if (!overall)
            whereClause = whereClause.and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))


        val t = TEAMS.`as`("t")
        val o = TEAMS.`as`("o")

        DriverManager.getConnection(
            databaseConnection.connectionString,
            databaseConnection.userName,
            databaseConnection.password
        ).use { conn ->
            try {
                val context = using(conn, databaseConnection.dialect)

                val q = context.with(
                    "cte"
                ).`as`(
                    select(
                        PLAYERSMATCHES.FULLNAME,
                        PLAYERSMATCHES.SORTNAMEPART,
                        BOWLINGDETAILS.NAME,
                        BOWLINGDETAILS.INNINGSORDER,
                        BOWLINGDETAILS.matches.BALLSPEROVER,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BOWLINGDETAILS.TEAMID,
                        rowNumber().over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                            BOWLINGDETAILS.PLAYERID
                        ).`as`("rn"),
                        coalesce(
                            sum(BOWLINGDETAILS.BALLS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("balls"),
                        coalesce(
                            sum(BOWLINGDETAILS.MAIDENS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("maidens"),
                        coalesce(
                            sum(BOWLINGDETAILS.WICKETS).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("wickets"),
                        coalesce(
                            sum(BOWLINGDETAILS.RUNS).over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
                                .orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("runs"),
                        coalesce(
                            sum(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over()
                                .partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                                    BOWLINGDETAILS.PLAYERID
                                )
                        ).`as`("SyntheticBestBowling"),
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                        .join(PLAYERSMATCHES).on(
                            PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID).and(
                                PLAYERSMATCHES.MATCHID.eq(
                                    BOWLINGDETAILS.MATCHID
                                )
                            )
                        )
                        .join(t).on(t.ID.eq(BOWLINGDETAILS.TEAMID))
                        .join(o).on(o.ID.eq(BOWLINGDETAILS.OPPONENTSID))
                        .where(whereClause)
                ).with("cte2").`as`(
                    select(
                        field("teamid"),
                        field("fullname"),
                        field("sortnamepart"),
                        field("seriesdate"),
                        field("matchstartdate"),
                        field("matchstartdateasoffset"),
                        field("location"),
                        field("ballsperover"),
                        field("balls"),
                        field("maidens"),
                        field("wickets"),
                        field("runs"),
                        field("SyntheticBestBowling"),
                        max(field("syntheticbestbowling")).over()
                            .orderBy(
                                field("matchstartdateasoffset"),
                                field("syntheticbestbowling").desc()
                            )
                            .rowsBetweenUnboundedPreceding().andCurrentRow().`as`("premax")
                    ).from("cte")
                        .where(field("SyntheticBestBowling").isNotNull)
                        .and(field("rn").eq(1))
                        .and(field("cte.wickets").gt(5))

                )

                val query = q.select(
                    field("fullname"),
                    field("sortnamepart"),
                    field("seriesdate"),
                    field("matchstartdate"),
                    field("matchstartdateasoffset"),
                    field("location"),
                    field("ballsperover"),
                    field("balls"),
                    field("maidens"),
                    field("wickets"),
                    field("runs"),
                    field("SyntheticBestBowling"),
                    field("name")
                )
                    .from("cte2")
                    .join(TEAMS).on(TEAMS.ID.eq(field("cte2.teamid", Int::class.java)))
                    .where(field("SyntheticBestBowling").ge(field("premax")))
                    .orderBy(field("SyntheticBestBowling"), field("matchstartdateasoffset"))

                val result = query.fetch()

                var previous = 0.0
                for (row in result) {

                    val current = row.getValue("SyntheticBestBowling", Double::class.java)
                    // want only one but there may be multiple scores with the same value
                    if (previous <= current) {
                        val date = row.getValue("matchstartdate", String::class.java)
                        val dateTime = java.time.LocalDate.parse(date, inputFormatter)
                        val matchDate = dateTime.format(outputFormatter)

                        val bb = BestBowlingDto(
                            row.getValue("fullname", String::class.java),
                            teamParams.team,
                            row.getValue("name", String::class.java),
                            row.getValue("ballsperover", Int::class.java),
                            row.getValue("balls", Int::class.java),
                            row.getValue("maidens", Int::class.java),
                            row.getValue("wickets", Int::class.java),
                            row.getValue("runs", Int::class.java),
                            row.getValue("location").toString(),
                            matchDate
                        )
                        bestBowling.add(bb)
                    } else {
                        break
                    }
                    previous = current
                }
            } catch (e: Exception) {
                println("")
                throw e
            }
        }
        return bestBowling
    }

}

