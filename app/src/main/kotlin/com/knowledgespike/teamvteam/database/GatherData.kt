package com.knowledgespike.teamvteam.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.teamvteam.helpers.getWicket
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import org.jooq.*
import org.jooq.impl.DSL.*
import java.sql.DriverManager


class TeamRecords(private val userName: String, private val password: String, private val connectionString: String) {

    private val log by LoggerDelegate()

    fun getHighestTotals(
        teamParams: TeamParams
    ): List<TotalDto> {
        val highestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val cte = context
                .with("cte")
                .`as`(
                    select(
                        INNINGS.TOTAL,
                        max(INNINGS.TOTAL.add(INNINGS.WICKETS.div(10))).over().`as`("max_synth"),
                        INNINGS.TOTAL.add((INNINGS.WICKETS.div(10))).`as`("synth"),
                        INNINGS.WICKETS,
                        INNINGS.DECLARED,
                        INNINGS.matches.LOCATION,
                        INNINGS.matches.SERIESDATE,
                        INNINGS.matches.MATCHSTARTDATEASOFFSET,
                    )
                        .from(INNINGS)
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
                )

            val result = cte.select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte").where(field("max_synth").eq(field("synth")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (r in result) {
                val hs = TotalDto(
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("Total", Int::class.java),
                    r.getValue("Wickets", Int::class.java),
                    r.getValue("Declared", Boolean::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                highestTotals.add(hs)
            }
        }

        return highestTotals
    }

    fun getLowestAllOutTotals(
        teamParams: TeamParams
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context.with("cte").`as`(
                getLowestTotalSelect(teamParams)
                    .and(INNINGS.WICKETS.eq(10))
            ).select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte").where(field("min_total").eq(field("total")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (r in result) {
                val hs = TotalDto(
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("Total", Int::class.java),
                    r.getValue("Wickets", Int::class.java),
                    r.getValue("Declared", Boolean::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                lowestTotals.add(hs)
            }

        }
        return lowestTotals
    }

    fun getLowestCompleteTotals(
        teamParams: TeamParams
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context.with("cte").`as`(
                getLowestTotalSelect(teamParams)
                    .and(INNINGS.COMPLETE)
            ).select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte").where(field("min_total").eq(field("total")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (r in result) {
                val hs = TotalDto(
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("Total", Int::class.java),
                    r.getValue("Wickets", Int::class.java),
                    r.getValue("Declared", Boolean::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                lowestTotals.add(hs)
            }

        }
        return lowestTotals
    }


    fun getLowestIncompleteTotals(
        teamParams: TeamParams
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context.with("cte").`as`(
                getLowestTotalSelect(teamParams)
            ).select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte").where(field("min_total").eq(field("total")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (r in result) {
                val hs = TotalDto(
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("Total", Int::class.java),
                    r.getValue("Wickets", Int::class.java),
                    r.getValue("Declared", Boolean::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                lowestTotals.add(hs)
            }

        }
        return lowestTotals
    }

    private fun getLowestTotalSelect(
        teamParams: TeamParams
    ): SelectConditionStep<Record7<Int?, Int?, Int?, Boolean?, String?, String?, Long?>> {
        return select(
            INNINGS.TOTAL,
            min(INNINGS.TOTAL).over().`as`("min_total"),
            INNINGS.WICKETS,
            INNINGS.DECLARED,
            INNINGS.matches.LOCATION,
            INNINGS.matches.SERIESDATE,
            INNINGS.matches.MATCHSTARTDATEASOFFSET
        )
            .from(INNINGS)
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
    }

    fun getHighestIndividualScores(teamParams: TeamParams): List<HighestScoreDto> {
        val highestScores = mutableListOf<HighestScoreDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.SCORE,
                        max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                        BATTINGDETAILS.NOTOUT,
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("Score", Int::class.java),
                    field("NotOut", Boolean::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("max_score").eq(field("NotOutAdjustedScore")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val hs = HighestScoreDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("Score", Int::class.java),
                    r.getValue("NotOut", Boolean::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                highestScores.add(hs)
            }

        }

        return highestScores
    }

    fun getLowestIndividualStrikeRate(teamParams: TeamParams, ballsLimit: Int): List<StrikeRateDto> {
        val lowestStrikeRates = mutableListOf<StrikeRateDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.SCORE,
                        BATTINGDETAILS.BALLS,
                        min(round((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS) * 100, 2)).over().`as`("min_sr"),
                        round((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS) * 100, 2).`as`("sr"),
                        max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .and(BATTINGDETAILS.BALLS.greaterOrEqual(ballsLimit))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("sr", Double::class.java),
                    field("score", Int::class.java),
                    field("balls", Int::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("sr").eq(field("min_sr")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val hs = StrikeRateDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("sr", Double::class.java),
                    r.getValue("score", Int::class.java),
                    r.getValue("balls", Int::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                lowestStrikeRates.add(hs)
            }

        }

        return lowestStrikeRates
    }

    fun getHighestIndividualStrikeRate(teamParams: TeamParams, scoreLimit: Int = 0): List<StrikeRateDto> {
        val highestStrikeRates = mutableListOf<StrikeRateDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.SCORE,
                        BATTINGDETAILS.BALLS,
                        max(round((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS) * 100, 2)).over().`as`("max_sr"),
                        round((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS) * 100, 2).`as`("sr"),
                        max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .and(BATTINGDETAILS.SCORE.greaterOrEqual(scoreLimit))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("sr", Double::class.java),
                    field("score", Int::class.java),
                    field("balls", Int::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("sr").eq(field("max_sr")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val hs = StrikeRateDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("sr", Double::class.java),
                    r.getValue("score", Int::class.java),
                    r.getValue("balls", Int::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                highestStrikeRates.add(hs)
            }

        }

        return highestStrikeRates
    }

    fun getHighestIndividualSixes(teamParams: TeamParams): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        max(BATTINGDETAILS.SIXES).over().`as`("max_sixes"),
                        BATTINGDETAILS.SIXES.`as`("sixes"),
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("max_sixes", Double::class.java),
                    field("sixes", Int::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("sixes").eq(field("max_sixes")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val boundaries = BoundariesDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("max_sixes", Int::class.java),
                    0,
                    0,
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                mostBoundaries.add(boundaries)
            }

        }

        return mostBoundaries
    }

    fun getHighestIndividualBoundaries(teamParams: TeamParams): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.FOURS,
                        BATTINGDETAILS.SIXES,
                        max(BATTINGDETAILS.SIXES + BATTINGDETAILS.FOURS).over().`as`("max_boundaries"),
                        (BATTINGDETAILS.SIXES + BATTINGDETAILS.FOURS).`as`("boundaries"),
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("fours", Int::class.java),
                    field("sixes", Int::class.java),
                    field("max_boundaries", Int::class.java),
                    field("boundaries", Int::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("boundaries").eq(field("max_boundaries")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val boundaries = BoundariesDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("max_boundaries", Int::class.java),
                    r.getValue("fours", Int::class.java),
                    r.getValue("sixes", Int::class.java),
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                mostBoundaries.add(boundaries)
            }

        }

        return mostBoundaries
    }

    fun getHighestIndividualFours(teamParams: TeamParams): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        max(BATTINGDETAILS.FOURS).over().`as`("max_fours"),
                        BATTINGDETAILS.FOURS.`as`("fours"),
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE,
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    )
                        .from(BATTINGDETAILS)
                        .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                ).select(
                    field("FullName", String::class.java),
                    field("max_fours", Double::class.java),
                    field("fours", Int::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                    field("MatchStartDateAsOffset", String::class.java),
                ).from("cte").where(field("fours").eq(field("max_fours")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val boundaries = BoundariesDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("max_fours", Int::class.java),
                    0,
                    0,
                    r.getValue("Location").toString(),
                    r.getValue("SeriesDate").toString()
                )
                mostBoundaries.add(boundaries)
            }

        }

        return mostBoundaries
    }

    fun getBestBowlingInnings(teamParams: TeamParams): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val cte = context
                .with("cte").`as`(
                    select(
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.RUNS,
                        BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                        max(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over().`as`("max_bb"),
                        PLAYERSMATCHES.FULLNAME,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    ).from(BOWLINGDETAILS)
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
                        .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
                )
            val results = cte.select(
                field("Wickets", Int::class.java),
                field("Runs", Int::class.java),
                field("SyntheticBestBowling", Double::class.java),
                field("FullName", String::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte")
                .where(field("max_bb").eq(field("syntheticbestbowling")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (row in results) {
                // want only one but there may be multiple scores with the same value
                val bb = BestBowlingDto(
                    row.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("Wickets", Int::class.java),
                    row.getValue("Runs", Int::class.java),
                    row.getValue("Location").toString(),
                    row.getValue("SeriesDate").toString()
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getBestBowlingStrikeRate(teamParams: TeamParams, oversLimit: Int = 0): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val cte = context
                .with("cte").`as`(
                    select(
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.OVERS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.RUNS,
                        round(min(BOWLINGDETAILS.BALLS / BOWLINGDETAILS.WICKETS).over(), 2).`as`("min_sr"),
                        round(BOWLINGDETAILS.BALLS / BOWLINGDETAILS.WICKETS, 2).`as`("sr"),
                        PLAYERSMATCHES.FULLNAME,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(MATCHES.ID.eq(BOWLINGDETAILS.MATCHID))
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
                        .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                        .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
                )
            val results = cte.select(
                field("Overs", String::class.java),
                field("Maidens", Int::class.java),
                field("Balls", Int::class.java),
                field("Wickets", Int::class.java),
                field("Runs", Int::class.java),
                field("sr", Double::class.java),
                field("FullName", String::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte")
                .where(field("min_sr").eq(field("sr")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (row in results) {
                // want only one but there may be multiple scores with the same value
                val bb = BowlingRatesDto(
                    row.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("Overs", String::class.java),
                    row.getValue("Balls", Int::class.java),
                    row.getValue("Maidens", Int::class.java),
                    row.getValue("Wickets", Int::class.java),
                    row.getValue("Runs", Int::class.java),
                    row.getValue("sr", Double::class.java),
                    row.getValue("Location").toString(),
                    row.getValue("SeriesDate").toString()
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getBestBowlingEconRate(teamParams: TeamParams, oversLimit: Int = 0): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val cte = context
                .with("cte").`as`(
                    select(
                        MATCHES.BALLSPEROVER,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.OVERS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.RUNS,
                        round(
                            min((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS) * MATCHES.BALLSPEROVER).over(),
                            2
                        ).`as`("min_er"),
                        round((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS) * MATCHES.BALLSPEROVER, 2).`as`("er"),
                        PLAYERSMATCHES.FULLNAME,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(MATCHES.ID.eq(BOWLINGDETAILS.MATCHID))
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
                        .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                        .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
                )
            val results = cte.select(
                field("BallsPerOver", Int::class.java),
                field("Wickets", Int::class.java),
                field("Overs", String::class.java),
                field("Maidens", Int::class.java),
                field("Runs", Int::class.java),
                field("Balls", Int::class.java),
                field("min_er", Int::class.java),
                field("er", Double::class.java),
                field("FullName", String::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte")
                .where(field("min_er").eq(field("er")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (row in results) {
                // want only one but there may be multiple scores with the same value
                val bb = BowlingRatesDto(
                    row.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("Overs", String::class.java),
                    row.getValue("Balls", Int::class.java),
                    row.getValue("Maidens", Int::class.java),
                    row.getValue("Wickets", Int::class.java),
                    row.getValue("Runs", Int::class.java),
                    row.getValue("er", Double::class.java),
                    row.getValue("Location").toString(),
                    row.getValue("SeriesDate").toString()
                )
                bestBowling.add(bb)
            }
        }
        return bestBowling
    }

    fun getWorstBowlingEconRate(teamParams: TeamParams, oversLimit: Int = 0): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val cte = context
                .with("cte").`as`(
                    select(
                        MATCHES.BALLSPEROVER,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.OVERS,
                        BOWLINGDETAILS.BALLS,
                        BOWLINGDETAILS.MAIDENS,
                        BOWLINGDETAILS.RUNS,
                        round(
                            max((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS) * MATCHES.BALLSPEROVER).over(),
                            2
                        ).`as`("max_er"),
                        round((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS) * MATCHES.BALLSPEROVER, 2).`as`("er"),
                        PLAYERSMATCHES.FULLNAME,
                        BOWLINGDETAILS.matches.LOCATION,
                        BOWLINGDETAILS.matches.SERIESDATE,
                        BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                    ).from(BOWLINGDETAILS)
                        .join(MATCHES).on(MATCHES.ID.eq(BOWLINGDETAILS.MATCHID))
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
                        .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                        .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
                )
            val results = cte.select(
                field("BallsPerOver", Int::class.java),
                field("Wickets", Int::class.java),
                field("Overs", String::class.java),
                field("Maidens", Int::class.java),
                field("Runs", Int::class.java),
                field("Balls", Int::class.java),
                field("max_er", Int::class.java),
                field("er", Double::class.java),
                field("FullName", String::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
                field("MatchStartDateAsOffset", String::class.java),
            ).from("cte")
                .where(field("max_er").eq(field("er")))
                .orderBy(field("MatchStartDateAsOffset"))
                .fetch()

            for (row in results) {
                // want only one but there may be multiple scores with the same value
                val bb = BowlingRatesDto(
                    row.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("Overs", String::class.java),
                    row.getValue("Balls", Int::class.java),
                    row.getValue("Maidens", Int::class.java),
                    row.getValue("Wickets", Int::class.java),
                    row.getValue("Runs", Int::class.java),
                    row.getValue("er", Double::class.java),
                    row.getValue("Location").toString(),
                    row.getValue("SeriesDate").toString()
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

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)

            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERSMATCHES.FULLNAME,
                    PLAYERSMATCHES.SORTNAMEPART,
                    BOWLINGDETAILS.NAME,
                    BOWLINGDETAILS.matches.LOCATION,
                    BOWLINGDETAILS.matches.SERIESDATE,
                    BOWLINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                    rowNumber().over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID).orderBy(
                        BOWLINGDETAILS.PLAYERID
                    ).`as`("rn"),
                    coalesce(
                        sum(BOWLINGDETAILS.WICKETS).over().partitionBy(BOWLINGDETAILS.MATCHID, BOWLINGDETAILS.PLAYERID)
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
                    ).`as`("synbb"),
                ).from(BOWLINGDETAILS)
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
                    field("FullName"),
                    field("SeriesDate"),
                    field("MatchStartDateAsOffset"),
                    field("Location"),
                    field("Wickets"),
                    field("Runs"),
                    field("synbb"),
                    max(field("synbb")).over().`as`("max_synbb")
                ).from("cte")
                    .where(field("synbb").isNotNull).and(field("rn").eq(1))
                    .orderBy(field("MatchStartDateAsOffset"))
            )

            val query = q.select().from("cte2").where(field("synbb").eq(field("max_synbb")))

            val result = query.fetch()

            var previous = 0.0
            for (r in result) {

                val current = r.getValue("synbb", Double::class.java)
                // want only one but there may be multiple scores with the same value
                if (previous <= current) {
                    val hs = BestBowlingDto(
                        r.getValue("FullName", String::class.java),
                        teamParams.team,
                        teamParams.opponents,
                        r.getValue("Wickets", Int::class.java),
                        r.getValue("Runs", Int::class.java),
                        r.getValue("Location").toString(),
                        r.getValue("SeriesDate").toString()
                    )
                    bestBowling.add(hs)
                } else {
                    break
                }
                previous = current
            }
        }
        return bestBowling
    }

    fun getMostRuns(teamParams: TeamParams): MutableList<MostRunsDto> {
        val mostRuns = mutableListOf<MostRunsDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)
            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERS.FULLNAME,
                    PLAYERS.SORTNAMEPART,
                    sum(`when`(BATTINGDETAILS.INNINGSNUMBER.eq(1), 1)).over().partitionBy(BATTINGDETAILS.PLAYERID)
                        .`as`("matches"),
                    count(BATTINGDETAILS.SCORE).over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("innings"),
                    sum(BATTINGDETAILS.SCORE).over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("runs"),
                    count().filterWhere(BATTINGDETAILS.NOTOUT).over().partitionBy(BATTINGDETAILS.PLAYERID)
                        .`as`("notouts"),
                    max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("hs"),
                    rowNumber().over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("rn")
                ).from(MATCHES)
                    .join(BATTINGDETAILS).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .join(PLAYERS).on(PLAYERS.ID.eq(BATTINGDETAILS.PLAYERID))
                    .where(
                        MATCHES.ID.`in`(
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
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

            ).with("cte2").`as`(
                select(
                    field("FullName"),
                    field("SortNamePart"),
                    field("matches"),
                    field("runs"),
                    max(field("runs")).over().`as`("max_runs"),
                    field("innings"),
                    field("notouts"),
                    trunc(
                        field("runs", Double::class.java)
                            .divide(
                                (field("innings", Int::class.java).subtract(
                                    field(
                                        "notouts",
                                        Int::class.java
                                    )
                                ))
                            ), 2
                    ).`as`("avg"),
                    field("hs"),
                ).from("cte")
                    .where(field("rn").eq(1))
            )

            val query = q.select()
                .from("cte2")
                .where(field("runs").eq(field("max_runs")))
                .orderBy(field("runs").desc(), field("SortNamePart"))

            val result = query.fetch()

            for (r in result) {
                val mr = MostRunsDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("matches", Int::class.java),
                    r.getValue("runs", Int::class.java),
                    r.getValue("innings", Int::class.java),
                    r.getValue("notouts", Int::class.java),
                    r.getValue("avg", Double::class.java),
                    getHighestScore(r.getValue("hs", Double::class.java))
                )
                mostRuns.add(mr)
            }
        }

        return mostRuns
    }

    fun getMostWickets(teamParams: TeamParams): MutableList<MostWicketsDto> {
        val mostWickets = mutableListOf<MostWicketsDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)

            val ctebb = name("ctebb")
                .fields("playerid", "runs", "wickets", "bb")
                .`as`(
                    select(
                        BOWLINGDETAILS.PLAYERID,
                        BOWLINGDETAILS.RUNS,
                        BOWLINGDETAILS.WICKETS,
                        BOWLINGDETAILS.SYNTHETICBESTBOWLING
                    ).from(BOWLINGDETAILS)
                        .where(
                            BOWLINGDETAILS.MATCHID.`in`(
                                select(MATCHSUBTYPE.MATCHID).from(
                                    MATCHSUBTYPE.where(
                                        MATCHSUBTYPE.MATCHTYPE.eq(
                                            teamParams.matchSubType
                                        )
                                    )
                                )
                            )
                        ).and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                )


            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERS.FULLNAME,
                    PLAYERS.SORTNAMEPART,
                    sum(`when`(BOWLINGDETAILS.INNINGSNUMBER.eq(1), 1)).over().partitionBy(BOWLINGDETAILS.PLAYERID)
                        .`as`("matches"),
                    sum(BOWLINGDETAILS.BALLS).over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("balls"),
                    sum(BOWLINGDETAILS.MAIDENS).over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("maidens"),
                    sum(BOWLINGDETAILS.RUNS).over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("runs"),
                    sum(BOWLINGDETAILS.WICKETS).over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("wickets"),
                    max(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("bb"),
                    rowNumber().over().partitionBy(BOWLINGDETAILS.PLAYERID).`as`("rn")
                ).from(MATCHES)
                    .join(BOWLINGDETAILS).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .join(PLAYERS).on(
                        PLAYERS.ID.eq(BOWLINGDETAILS.PLAYERID)
                    )
                    .where(
                        MATCHES.ID.`in`(
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
                    .and(BOWLINGDETAILS.BALLS.notEqual(0))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

            ).with(ctebb)
                .with("cte2").`as`(
                    select(
                        field("FullName"),
                        field("SortNamePart"),
                        field("matches"),
                        field("balls"),
                        field("maidens"),
                        field("cte.runs"),
                        field("cte.wickets"),
                        field("cte.bb"),
                        max(field("cte.wickets")).over().`as`("max_wickets"),
                        field("ctebb.wickets").`as`("bbwickets"),
                        field("ctebb.runs").`as`("bbruns"),
                        trunc(
                            field("cte.runs", Double::class.java)
                                .divide(
                                    (field("cte.wickets", Int::class.java))
                                ), 2
                        ).`as`("avg")
                    ).from("cte")
                        .join(ctebb)
                        .on(ctebb.field("playerid", Int::class.java)?.eq(field("playerid", Int::class.java)))
                        .and(ctebb.field("bb", Double::class.java)?.eq(field("cte.bb", Double::class.java)))
                        .where(field("rn").eq(1))
                )

            val query = q.select()
                .from("cte2")
                .where(field("wickets").eq(field("max_wickets")))
                .orderBy(field("wickets").desc(), field("SortNamePart"))

            val result = query.fetch()

            for (r in result) {
                val mr = MostWicketsDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("matches", Int::class.java),
                    r.getValue("balls", Int::class.java),
                    r.getValue("maidens", Int::class.java),
                    r.getValue("runs", Int::class.java),
                    r.getValue("wickets", Int::class.java),
                    r.getValue("bbruns", Int::class.java),
                    r.getValue("bbwickets", Int::class.java),
                    r.getValue("avg", Double::class.java)
                )
                mostWickets.add(mr)
            }
        }

        return mostWickets
    }

    fun getMostCatches(teamParams: TeamParams): MutableList<MostDismissalsDto> {
        val mostCatches = mutableListOf<MostDismissalsDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)

            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERS.FULLNAME,
                    PLAYERS.SORTNAMEPART,
                    sum(`when`(FIELDING.INNINGSNUMBER.eq(1), 1)).over().partitionBy(FIELDING.PLAYERID).`as`("matches"),
                    sum(FIELDING.CAUGHTF).over().partitionBy(FIELDING.PLAYERID).add(
                        sum(FIELDING.CAUGHTWK).over().partitionBy(FIELDING.PLAYERID)
                    ).`as`("caught"),
                    rowNumber().over().partitionBy(FIELDING.PLAYERID).`as`("rn")
                ).from(MATCHES)
                    .join(FIELDING).on(FIELDING.MATCHID.eq(MATCHES.ID))
                    .join(PLAYERS).on(
                        PLAYERS.ID.eq(FIELDING.PLAYERID)
                    )
                    .where(
                        MATCHES.ID.`in`(
                            select(MATCHSUBTYPE.MATCHID).from(
                                MATCHSUBTYPE.where(
                                    MATCHSUBTYPE.MATCHTYPE.eq(
                                        teamParams.matchSubType
                                    )
                                )
                            )
                        )
                    )
                    .and(MATCHES.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(FIELDING.TEAMID.`in`(teamParams.teamIds))
                    .and(FIELDING.OPPONENTSID.`in`(teamParams.opponentIds))

            ).with("cte2").`as`(
                select(
                    field("FullName"),
                    field("SortNamePart"),
                    field("matches"),
                    field("caught"),
                    max(field("caught")).over().`as`("max_caught")
                ).from("cte")
                    .where(field("rn").eq(1))
                    .and(field("caught").gt(0))
            )

            val query = q.select()
                .from("cte2")
                .where(field("caught").eq(field("max_caught")))
                .orderBy(field("caught").desc(), field("SortNamePart"))

            val result = query.fetch()

            for (r in result) {
                val mr = MostDismissalsDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("matches", Int::class.java),
                    r.getValue("caught", Int::class.java)
                )
                mostCatches.add(mr)
            }
        }

        return mostCatches
    }

    fun getMostStumpings(teamParams: TeamParams): MutableList<MostDismissalsDto> {
        val mostStumpings = mutableListOf<MostDismissalsDto>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)

            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERS.FULLNAME,
                    PLAYERS.SORTNAMEPART,
                    sum(`when`(FIELDING.INNINGSNUMBER.eq(1), 1)).over().partitionBy(FIELDING.PLAYERID).`as`("matches"),
                    sum(FIELDING.STUMPED).over().partitionBy(FIELDING.PLAYERID).`as`("stumpings"),
                    rowNumber().over().partitionBy(FIELDING.PLAYERID).`as`("rn")
                ).from(MATCHES)
                    .join(FIELDING).on(FIELDING.MATCHID.eq(MATCHES.ID))
                    .join(PLAYERS).on(
                        PLAYERS.ID.eq(FIELDING.PLAYERID)
                    )
                    .where(
                        MATCHES.ID.`in`(
                            select(MATCHSUBTYPE.MATCHID).from(
                                MATCHSUBTYPE.where(
                                    MATCHSUBTYPE.MATCHTYPE.eq(
                                        teamParams.matchSubType
                                    )
                                )
                            )
                        )
                    )
                    .and(MATCHES.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(FIELDING.TEAMID.`in`(teamParams.teamIds))
                    .and(FIELDING.OPPONENTSID.`in`(teamParams.opponentIds))

            ).with("cte2").`as`(
                select(
                    field("FullName"),
                    field("SortNamePart"),
                    field("matches"),
                    field("stumpings"),
                    max(field("stumpings")).over().`as`("max_stumpings")
                ).from("cte")
                    .where(field("rn").eq(1))
                    .and(field("stumpings").gt(0))
            )

            val query = q.select()
                .from("cte2")
                .where(field("stumpings").eq(field("max_stumpings")))
                .orderBy(field("stumpings").desc(), field("SortNamePart"))

            val result = query.fetch()

            for (r in result) {
                val mr = MostDismissalsDto(
                    r.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("matches", Int::class.java),
                    r.getValue("stumpings", Int::class.java)
                )
                mostStumpings.add(mr)
            }
        }

        return mostStumpings
    }

    private fun getHighestScore(value: Double?): String {
        if (value == null) return ""
        if (value.rem(1) == 0.0) return value.toInt().toString()
        return "${(value - 0.5).toInt()}*"
    }


    fun getHighestFoW(teamParams: TeamParams): MutableMap<Int, FowDetails> {

        val bestFow = mutableMapOf<Int, FowDetails>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = using(conn, SQLDialect.MYSQL)

            for (wicket in 1..10) {
                val listFoW = mutableListOf<FoWDto>()
                val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()
                val selectFoW = createFowSelectForGivenWicketAndTeams(teamParams, wicket)


                val query = context
                    .with("cte").`as`(
                        selectFoW
                    ).with("cte2").`as`(
                        select().from("cte").where(field("synth_partnership").eq(field("max_partnership")))
                            .and(field("rn").eq(1))
                            .orderBy(field("MatchStartDateAsOffset"))
                    )
                val result = query.select().from("cte2").fetch()


                for (partnershipRecord in result) {

                    // want only one but there may be multiple scores with the same value
                    val fow = FoWDto(
                        teamParams.team,
                        teamParams.opponents,
                        partnershipRecord.getValue("Location", String::class.java),
                        partnershipRecord.getValue("SeriesDate", String::class.java),
                        partnershipRecord.getValue("Partnership", Int::class.java),
                        partnershipRecord.getValue("Wicket", Int::class.java),
                        partnershipRecord.getValue("Unbroken", Boolean::class.java),
                        partnershipRecord.getValue("FullName", String::class.java) ?: "unknown",
                        partnershipRecord.getValue("Score", Int::class.java),
                        partnershipRecord.getValue("NotOut", Boolean::class.java),
                        partnershipRecord.getValue("fullName2", String::class.java) ?: "unknown",
                        partnershipRecord.getValue("score2", Int::class.java),
                        partnershipRecord.getValue("notout2", Boolean::class.java),
                    )

                    listMultiPlayerFowDao.addAll(
                        getMultiplePlayerFow(
                            fow.wicket,
                            fow.partnership,
                            teamParams,
                            context
                        )
                    )

                    listFoW.add(fow)
                }
                bestFow[wicket] = FowDetails(listFoW, listMultiPlayerFowDao)

            }

        }
        return bestFow
    }

    private fun getMultiplePlayerFow(
        wicket: Int,
        partnership: Int,
        teamParams: TeamParams,
        context: DSLContext
    ): MutableList<MultiPlayerFowDto> {

        val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()
        val possibleMatches = isMultiPlayerPartnership(partnership, wicket, teamParams, context)
        possibleMatches.forEach { match ->
            val selectMultiple = getMultiPlayerPartnershipForGivenWicket(
                match.matchId,
                wicket,
                teamParams,
                match.inningsOrder
            )

            val queryMultiples = context
                .with("cte").`as`(
                    selectMultiple
                ).with("cte2").`as`(
                    select().from("cte").where(field("rn").eq(1))
                        .orderBy(field("MatchStartDateAsOffset"))

                )
            val resultMultiples = queryMultiples.select().from("cte2")
                .fetch()

            if (resultMultiples.size > 1) {
                val listMultiPlayerFow = mutableListOf<FoWDto>()

                val possibleBestPartnership = resultMultiples.get(0).getValue("Partnership", Int::class.java)

                if (possibleBestPartnership >= partnership) {
                    log.info(
                        "Have a possible match for multiple wicket record ${teamParams.team} vs ${teamParams.opponents} for the ${
                            getWicket(
                                wicket
                            )
                        } wicket"
                    )

                    var total = 0

                    for ((index, rmultiple) in resultMultiples.withIndex()) {

                        // the first entry is the total partnership
                        if (index == 0) {
                            total = rmultiple.getValue("Partnership", Int::class.java)
                            continue
                        }

                        val multiPlayerFow = FoWDto(
                            teamParams.team,
                            teamParams.opponents,
                            rmultiple.getValue("Location", String::class.java),
                            rmultiple.getValue("SeriesDate", String::class.java),
                            rmultiple.getValue("Partnership", Int::class.java),
                            rmultiple.getValue("Wicket", Int::class.java),
                            rmultiple.getValue("Unbroken", Boolean::class.java),
                            rmultiple.getValue("FullName", String::class.java) ?: "unknown",
                            rmultiple.getValue("Score", Int::class.java),
                            rmultiple.getValue("NotOut", Boolean::class.java),
                            rmultiple.getValue("fullName2", String::class.java) ?: "unknown",
                            rmultiple.getValue("score2", Int::class.java),
                            rmultiple.getValue("notout2", Boolean::class.java),
                        )

                        listMultiPlayerFow.add(multiPlayerFow)
                    }

                    listMultiPlayerFowDao.add(MultiPlayerFowDto(total, wicket, listMultiPlayerFow))
                }
            }
        }
        return listMultiPlayerFowDao
    }

    private fun isMultiPlayerPartnership(
        partnership: Int,
        wicket: Int,
        teamParams: TeamParams,
        context: DSLContext
    ): List<PossibleMultiPlayerPartnerships> {

        val possibleMatches = mutableListOf<PossibleMultiPlayerPartnerships>()

        val result = context.select(PARTNERSHIPS.MATCHID, PARTNERSHIPS.INNINGSORDER)
            .from(PARTNERSHIPS)
            .where(PARTNERSHIPS.WICKET.eq(wicket))
            .and(PARTNERSHIPS.PARTNERSHIP.ge(partnership))
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            .and(PARTNERSHIPS.MATCHTYPE.eq(teamParams.matchType))
            .and(PARTNERSHIPS.MULTIPLE.eq(true))
            .and(
                PARTNERSHIPS.MATCHID.`in`(
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
            .fetch()
        possibleMatches.addAll(result.map { r ->
            PossibleMultiPlayerPartnerships(
                r.getValue("MatchId", Int::class.java),
                r.getValue(
                    "InningsOrder", Int::class.java
                )
            )
        })

        return possibleMatches
    }

    private fun createFowSelectForGivenWicketAndTeams(
        teamParams: TeamParams,
        wicket: Int
    ): SelectSeekStep3<Record18<Int?, Int?, Int?, Int?, Boolean?, String?, String?, String?, Long?, Int?, Int?, Int, String?, Int?, Boolean?, String?, Int?, Boolean?>, Int?, Boolean?, Int?> {

        return select(
            PARTNERSHIPS.matches.ID,
            PARTNERSHIPS.WICKET,
            PARTNERSHIPS.INNINGSORDER,
            PARTNERSHIPS.PARTNERSHIP,
            PARTNERSHIPS.UNBROKEN,
            PARTNERSHIPS.PLAYERNAMES,
            PARTNERSHIPS.matches.LOCATION,
            PARTNERSHIPS.matches.SERIESDATE,
            PARTNERSHIPS.matches.MATCHSTARTDATEASOFFSET,
            (PARTNERSHIPS.PARTNERSHIP + PARTNERSHIPS.UNBROKEN.div(10)).`as`("synth_partnership"),
            max(PARTNERSHIPS.PARTNERSHIP + PARTNERSHIPS.UNBROKEN.div(10)).over().`as`("max_partnership"),
            rowNumber().over().partitionBy(PARTNERSHIPS.matches.ID, PARTNERSHIPS.PARTNERSHIP).`as`("rn"),
            PLAYERSMATCHES.FULLNAME,
            BATTINGDETAILS.SCORE,
            BATTINGDETAILS.NOTOUT,
            lead(PLAYERSMATCHES.FULLNAME).over().partitionBy(PARTNERSHIPS.matches.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("fullName2"),
            lead(BATTINGDETAILS.SCORE).over().partitionBy(PARTNERSHIPS.matches.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("score2"),
            lead(BATTINGDETAILS.NOTOUT).over().partitionBy(PARTNERSHIPS.matches.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("notout2"),
        )
            .from(PARTNERSHIPS)
            .leftOuterJoin(PARTNERSHIPSPLAYERS).on(PARTNERSHIPSPLAYERS.PARTNERSHIPID.eq(PARTNERSHIPS.ID))
            .leftOuterJoin(PLAYERSMATCHES).on(
                PLAYERSMATCHES.PLAYERID.eq(PARTNERSHIPSPLAYERS.PLAYERID).and(
                    PLAYERSMATCHES.MATCHID.eq(PARTNERSHIPS.matches.ID)
                )
            )
            .leftOuterJoin(BATTINGDETAILS).on(
                BATTINGDETAILS.MATCHID.eq(PARTNERSHIPS.matches.ID).and(
                    BATTINGDETAILS.PLAYERID.eq(
                        PARTNERSHIPSPLAYERS.PLAYERID
                    ).and(BATTINGDETAILS.INNINGSORDER.eq(PARTNERSHIPS.INNINGSORDER))
                )
            )
            .where(PARTNERSHIPS.matches.MATCHTYPE.eq(teamParams.matchType))
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
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            .and(PARTNERSHIPS.MULTIPLE.eq(false))
            .and(PARTNERSHIPS.WICKET.eq(wicket))
            .orderBy(PARTNERSHIPS.PARTNERSHIP.desc(), PARTNERSHIPS.UNBROKEN.desc(), PARTNERSHIPS.matches.ID)
    }


    private fun getMultiPlayerPartnershipForGivenWicket(
        matchId: Int,
        wicket: Int,
        teamParams: TeamParams,
        inningsOrder: Int
    ): SelectSeekStep2<Record14<Int, String?, String?, Long?, Int?, Int?, Boolean?, Boolean?, String?, Int?, Boolean?, String?, Int?, Boolean?>, Int?, Boolean?> {
        return select(
            rowNumber().over().partitionBy(PARTNERSHIPS.PLAYERIDS).`as`("rn"),
            PARTNERSHIPS.matches.LOCATION,
            PARTNERSHIPS.matches.SERIESDATE,
            PARTNERSHIPS.matches.MATCHSTARTDATEASOFFSET,
            PARTNERSHIPS.PARTNERSHIP,
            PARTNERSHIPS.WICKET,
            PARTNERSHIPS.UNBROKEN,
            PARTNERSHIPS.MULTIPLE,
            PLAYERSMATCHES.FULLNAME,
            BATTINGDETAILS.SCORE,
            BATTINGDETAILS.NOTOUT,
            lead(PLAYERSMATCHES.FULLNAME).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("fullName2"),
            lead(BATTINGDETAILS.SCORE).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("score2"),
            lead(BATTINGDETAILS.NOTOUT).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("notout2"),
        ).from(PARTNERSHIPS)
            .join(PARTNERSHIPSPLAYERS).on(PARTNERSHIPS.ID.eq(PARTNERSHIPSPLAYERS.PARTNERSHIPID))
            .leftOuterJoin(PLAYERSMATCHES).on(
                PLAYERSMATCHES.PLAYERID.eq(PARTNERSHIPSPLAYERS.PLAYERID).and(
                    PLAYERSMATCHES.MATCHID.eq(
                        PARTNERSHIPS.matches.ID
                    )
                )
            )
            .leftOuterJoin(BATTINGDETAILS).on(
                BATTINGDETAILS.MATCHID.eq(matchId).and(
                    BATTINGDETAILS.PLAYERID.eq(
                        PARTNERSHIPSPLAYERS.PLAYERID
                    ).and(BATTINGDETAILS.INNINGSORDER.eq(PARTNERSHIPS.INNINGSORDER))
                )
            )
            .where(PARTNERSHIPS.MATCHID.eq(matchId))
            .and(PARTNERSHIPS.WICKET.eq(wicket))
            .and(PARTNERSHIPS.INNINGSORDER.eq(inningsOrder))
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            .orderBy(PARTNERSHIPS.PARTNERSHIP.desc(), PARTNERSHIPS.UNBROKEN.desc())
    }
}

data class TeamParams(
    val teamIds: List<Int>,
    val opponentIds: List<Int>,
    val team: String,
    val opponents: String,
    val matchType: String,
    val matchSubType: String,
)



