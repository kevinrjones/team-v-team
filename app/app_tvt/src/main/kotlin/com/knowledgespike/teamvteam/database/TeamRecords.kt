package com.knowledgespike.teamvteam.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.shared.data.*
import com.knowledgespike.shared.database.getPossibleFallOfWicketMissingPartnerships
import com.knowledgespike.shared.logging.LoggerDelegate
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.shared.html.getWicket
import org.jooq.*
import org.jooq.impl.DSL.*
import java.sql.Connection
import java.sql.DriverManager


class TeamRecords(private val connection: Connection, private val dialect: SQLDialect) {

    private val log by LoggerDelegate()

    fun getHighestTotals(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
    ): List<TotalDto> {
        val highestTotals = mutableListOf<TotalDto>()

        val context = using(connection, dialect)

        var whereClause = INNINGS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))
            .and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val cte = context
            .with("cte")
            .`as`(
                select(
                    INNINGS.TOTAL,
                    max(INNINGS.TOTAL.add(INNINGS.WICKETS.cast(Float::class.java).div(10))).over()
                        .`as`("max_synth"),
                    INNINGS.TOTAL.add((INNINGS.WICKETS.cast(Float::class.java).div(10))).`as`("synth"),
                    INNINGS.WICKETS,
                    INNINGS.DECLARED,
                    INNINGS.COMPLETE,
                    INNINGS.ALLOUT,
                    INNINGS.matches.LOCATION,
                    INNINGS.matches.SERIESDATE,
                    INNINGS.matches.MATCHSTARTDATEASOFFSET,
                )
                    .from(INNINGS)
                    .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
                    .where(whereClause)
                    .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                    .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
            )

        val result = cte.select(
            field("total", Int::class.java),
            field("wickets", Int::class.java),
            field("declared", Boolean::class.java),
            field("allout", Boolean::class.java),
            field("complete", Boolean::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte").where(field("max_synth").eq(field("synth")))
            .orderBy(field("matchstartdateasoffset"))
            .fetch()

        for (r in result) {
            val hs = TotalDto(
                teamParams.team,
                teamParams.opponents,
                r.getValue("total", Int::class.java),
                r.getValue("wickets", Int::class.java),
                r.getValue("declared", Boolean::class.java),
                r.getValue("complete", Boolean::class.java),
                r.getValue("allout", Boolean::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            highestTotals.add(hs)
        }


        return highestTotals
    }

    fun getLowestAllOutTotals(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()


        val context = using(connection, dialect)
        val result = context.with("cte").`as`(
            getLowestTotalSelect(countryIds, teamParams, startFrom)
                .and(INNINGS.ALLOUT.eq(1))
        ).select(
            field("total", Int::class.java),
            field("wickets", Int::class.java),
            field("declared", Boolean::class.java),
            field("complete", Boolean::class.java),
            field("allout", Boolean::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte").where(field("min_total").eq(field("total")))
            .orderBy(field("matchstartdateasoffset"))
            .fetch()

        for (r in result) {
            val hs = TotalDto(
                teamParams.team,
                teamParams.opponents,
                r.getValue("total", Int::class.java),
                r.getValue("wickets", Int::class.java),
                r.getValue("declared", Boolean::class.java),
                r.getValue("complete", Boolean::class.java),
                r.getValue("allout", Boolean::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            lowestTotals.add(hs)
        }


        return lowestTotals
    }

    fun getLowestCompleteTotals(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()

        val context = using(connection, dialect)
        val result = context.with("cte").`as`(
            getLowestTotalSelect(countryIds, teamParams, startFrom)
                .and(INNINGS.COMPLETE.eq(1))
        ).select(
            field("total", Int::class.java),
            field("wickets", Int::class.java),
            field("declared", Boolean::class.java),
            field("allout", Boolean::class.java),
            field("complete", Boolean::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte").where(field("min_total").eq(field("total")))
            .orderBy(field("matchstartdateasoffset"))
            .fetch()

        for (r in result) {
            val hs = TotalDto(
                teamParams.team,
                teamParams.opponents,
                r.getValue("total", Int::class.java),
                r.getValue("wickets", Int::class.java),
                r.getValue("declared", Boolean::class.java),
                r.getValue("complete", Boolean::class.java),
                r.getValue("allout", Boolean::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            lowestTotals.add(hs)
        }


        return lowestTotals
    }


    fun getLowestIncompleteTotals(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
    ): List<TotalDto> {
        val lowestTotals = mutableListOf<TotalDto>()


        val context = using(connection, dialect)
        val result = context.with("cte").`as`(
            getLowestTotalSelect(countryIds, teamParams, startFrom)
        ).select(
            field("total", Int::class.java),
            field("wickets", Int::class.java),
            field("declared", Boolean::class.java),
            field("complete", Boolean::class.java),
            field("allout", Boolean::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte").where(field("min_total").eq(field("total")))
            .orderBy(field("matchstartdateasoffset"))
            .fetch()

        for (r in result) {
            val hs = TotalDto(
                teamParams.team,
                teamParams.opponents,
                r.getValue("total", Int::class.java),
                r.getValue("wickets", Int::class.java),
                r.getValue("declared", Boolean::class.java),
                r.getValue("complete", Boolean::class.java),
                r.getValue("allout", Boolean::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            lowestTotals.add(hs)
        }


        return lowestTotals
    }

    private fun getLowestTotalSelect(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
    ): SelectConditionStep<Record9<Int?, Int?, Int?, Byte?, Byte?, Byte?, String?, String?, Long?>> {

        var whereClause = INNINGS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))
            .and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        return select(
            INNINGS.TOTAL,
            min(INNINGS.TOTAL).over().`as`("min_total"),
            INNINGS.WICKETS,
            INNINGS.DECLARED,
            INNINGS.COMPLETE,
            INNINGS.ALLOUT,
            INNINGS.matches.LOCATION,
            INNINGS.matches.SERIESDATE,
            INNINGS.matches.MATCHSTARTDATEASOFFSET
        )
            .from(INNINGS)
            .join(MATCHES).on(INNINGS.MATCHID.eq(MATCHES.ID))
            .where(whereClause)
            .and(INNINGS.matches.MATCHTYPE.notIn(internationalMatchTypes))
            .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
            .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
    }

    fun getHighestIndividualScores(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<HighestScoreDto> {
        val highestscores = mutableListOf<HighestScoreDto>()

        val context = using(connection, dialect)

        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val result = context
            .with("cte1").`as`(
                select(
                    max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score")
                ).from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
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
                    .limit(1)
            )
            .with("cte2").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    BATTINGDETAILS.SCORE,
                    BATTINGDETAILS.NOTOUT,
                    BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
            ).select(
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("score", Int::class.java),
                field("notout", Boolean::class.java),
                field("location", String::class.java),
                field("seriesdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte2")
            .join("cte1")
            .on(field("max_score").eq(field("notoutAdjustedscore")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            // want only one but there may be multiple scores with the same value
            val hs = HighestScoreDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("score", Int::class.java),
                r.getValue("notout", Boolean::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            highestscores.add(hs)
        }



        return highestscores
    }

    fun getLowestIndividualStrikeRate(
        countryIds: List<Int>,
        teamParams: TeamParams,
        ballsLimit: Int,
        startFrom: Long
    ): List<StrikeRateDto> {
        val lowestStrikeRates = mutableListOf<StrikeRateDto>()

        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))


        val context = using(connection, dialect)
        val cte = context
            .with("cte").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    BATTINGDETAILS.SCORE,
                    BATTINGDETAILS.BALLS,
                    min(
                        (BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS.cast(Float::class.java)) * 100
                    ).over().`as`("min_sr"),
                    ((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS.cast(Float::class.java)) * 100)
                        .`as`("sr"),
                    max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.BALLS.greaterOrEqual(ballsLimit))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc(),
                        BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET,
                        BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART
                    )
            ).with("cte2").`as`(
                select(
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    round(field("min_sr", Double::class.java), 2).`as`("min_sr"),
                    round(field("sr", Double::class.java), 2).`as`("sr"),
                    field("score", Int::class.java),
                    field("balls", Int::class.java),
                    field("location", String::class.java),
                    field("seriesdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte")
            )

        val result = cte.select()
            .from("cte2")
            .where(field("sr").eq(field("min_sr")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            val hs = StrikeRateDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("sr", Double::class.java),
                r.getValue("score", Int::class.java),
                r.getValue("balls", Int::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            lowestStrikeRates.add(hs)
        }

        return lowestStrikeRates
    }

    fun getHighestIndividualStrikeRate(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
        scoreLimit: Int = 0,
    ): List<StrikeRateDto> {
        val highestStrikeRates = mutableListOf<StrikeRateDto>()


        val context = using(connection, dialect)
        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val cte = context
            .with("cte").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    BATTINGDETAILS.SCORE,
                    BATTINGDETAILS.BALLS,
                    max(

                        (BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS.cast(Float::class.java)) * 100
                    ).over().`as`("max_sr"),
                    ((BATTINGDETAILS.SCORE / BATTINGDETAILS.BALLS.cast(Float::class.java)) * 100)
                        .`as`("sr"),
                    max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.BALLS.gt(0))
                    .and(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.SCORE.greaterOrEqual(scoreLimit))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
            ).with("cte2").`as`(
                select(
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    round(field("max_sr", Double::class.java), 2).`as`("max_sr"),
                    round(field("sr", Double::class.java), 2).`as`("sr"),
                    field("score", Int::class.java),
                    field("balls", Int::class.java),
                    field("location", String::class.java),
                    field("seriesdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte")
            )

        val result = cte.select()
            .from("cte2").where(field("sr").eq(field("max_sr")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            // want only one but there may be multiple scores with the same value
            val hs = StrikeRateDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("sr", Double::class.java),
                r.getValue("score", Int::class.java),
                r.getValue("balls", Int::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            highestStrikeRates.add(hs)
        }

        return highestStrikeRates
    }

    fun getHighestIndividualSixes(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()

        val context = using(connection, dialect)

        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val result = context
            .with("cte").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    max(BATTINGDETAILS.SIXES).over().`as`("max_sixes"),
                    BATTINGDETAILS.SIXES.`as`("sixes"),
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
            ).select(
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("max_sixes", Double::class.java),
                field("sixes", Int::class.java),
                field("location", String::class.java),
                field("seriesdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte").where(field("sixes").eq(field("max_sixes")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            // want only one but there may be multiple scores with the same value
            val boundaries = BoundariesDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("max_sixes", Int::class.java),
                0,
                0,
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            mostBoundaries.add(boundaries)
        }

        return mostBoundaries
    }

    fun getHighestIndividualBoundaries(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()


        val context = using(connection, dialect)
        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val result = context
            .with("cte").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    BATTINGDETAILS.FOURS,
                    BATTINGDETAILS.SIXES,
                    max(BATTINGDETAILS.SIXES + BATTINGDETAILS.FOURS).over().`as`("max_boundaries"),
                    (BATTINGDETAILS.SIXES + BATTINGDETAILS.FOURS).`as`("boundaries"),
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
            ).select(
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("fours", Int::class.java),
                field("sixes", Int::class.java),
                field("max_boundaries", Int::class.java),
                field("boundaries", Int::class.java),
                field("location", String::class.java),
                field("seriesdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte").where(field("boundaries").eq(field("max_boundaries")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            // want only one but there may be multiple scores with the same value
            val boundaries = BoundariesDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("max_boundaries", Int::class.java),
                r.getValue("fours", Int::class.java),
                r.getValue("sixes", Int::class.java),
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            mostBoundaries.add(boundaries)
        }

        return mostBoundaries
    }

    fun getHighestIndividualFours(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<BoundariesDto> {
        val mostBoundaries = mutableListOf<BoundariesDto>()

        val context = using(connection, dialect)

        var whereClause = BATTINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val result = context
            .with("cte").`as`(
                select(
                    BATTINGDETAILS.FULLNAME,
                    BATTINGDETAILS.battingdetailsIbfk_2.SORTNAMEPART,
                    max(BATTINGDETAILS.FOURS).over().`as`("max_fours"),
                    BATTINGDETAILS.FOURS.`as`("fours"),
                    BATTINGDETAILS.matches.LOCATION,
                    BATTINGDETAILS.matches.SERIESDATE,
                    BATTINGDETAILS.matches.MATCHSTARTDATEASOFFSET
                )
                    .from(BATTINGDETAILS)
                    .join(MATCHES).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(BATTINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                    .and(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
            ).select(
                field("fullname", String::class.java),
                field("sortnamepart", String::class.java),
                field("max_fours", Double::class.java),
                field("fours", Int::class.java),
                field("location", String::class.java),
                field("seriesdate", String::class.java),
                field("matchstartdateasoffset", String::class.java),
            ).from("cte").where(field("fours").eq(field("max_fours")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()



        for (r in result) {
            // want only one but there may be multiple scores with the same value
            val boundaries = BoundariesDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("max_fours", Int::class.java),
                0,
                0,
                r.getValue("location").toString(),
                r.getValue("seriesdate").toString()
            )
            mostBoundaries.add(boundaries)
        }

        return mostBoundaries
    }

    fun getBestBowlingInnings(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<BestBowlingDto> {

        val bestBowling = mutableListOf<BestBowlingDto>()

        var whereClause = BOWLINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)

        val cte = context
            .with("cte1").`as`(
                select(
                    max(BOWLINGDETAILS.SYNTHETICBESTBOWLING).over().`as`("max_bb"),
                ).from(BOWLINGDETAILS)
                    .join(PLAYERSMATCHES).on(
                        PLAYERSMATCHES.PLAYERID.eq(BOWLINGDETAILS.PLAYERID)
                            .and(PLAYERSMATCHES.MATCHID.eq(BOWLINGDETAILS.MATCHID))
                    )
                    .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .where(BOWLINGDETAILS.matches.MATCHTYPE.eq(teamParams.matchType))
                    .and(whereClause)
                    .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .limit(1)
            )
            .with("cte2").`as`(
                select(
                    BOWLINGDETAILS.WICKETS,
                    BOWLINGDETAILS.RUNS,
                    BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                    PLAYERSMATCHES.FULLNAME,
                    PLAYERSMATCHES.SORTNAMEPART,
                    BOWLINGDETAILS.matches.LOCATION,
                    BOWLINGDETAILS.matches.SERIESDATE,
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
                    .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
            )
        val results = cte.select(
            field("wickets", Int::class.java),
            field("runs", Int::class.java),
            field("SyntheticBestBowling", Double::class.java),
            field("fullname", String::class.java),
            field("sortnamepart", String::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte2")
            .join("cte1").on(field("max_bb").eq(field("syntheticbestbowling")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()

        for (row in results) {
            // want only one but there may be multiple scores with the same value
            val bb = BestBowlingDto(
                row.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                row.getValue("wickets", Int::class.java),
                row.getValue("runs", Int::class.java),
                row.getValue("location").toString(),
                row.getValue("seriesdate").toString()
            )
            bestBowling.add(bb)
        }

        return bestBowling
    }

    fun getBestBowlingStrikeRate(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
        oversLimit: Int = 0,
    ): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        var whereClause = BOWLINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)
        val cte = context
            .with("cte").`as`(
                select(
                    BOWLINGDETAILS.WICKETS,
                    BOWLINGDETAILS.BALLS,
                    BOWLINGDETAILS.OVERS,
                    BOWLINGDETAILS.MAIDENS,
                    BOWLINGDETAILS.RUNS,
                    min(BOWLINGDETAILS.BALLS / BOWLINGDETAILS.WICKETS.cast(Float::class.java)).over()
                        .`as`("min_sr"),
                    (BOWLINGDETAILS.BALLS / BOWLINGDETAILS.WICKETS.cast(Float::class.java)).`as`("sr"),
                    PLAYERSMATCHES.FULLNAME,
                    PLAYERSMATCHES.SORTNAMEPART,
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
                    .and(field("wickets").ne(0))
                    .and(whereClause)
                    .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                    .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
            ).with("cte2").`as`(
                select(
                    field("overs", String::class.java),
                    field("maidens", Int::class.java),
                    field("balls", Int::class.java),
                    field("wickets", Int::class.java),
                    field("runs", Int::class.java),
                    round(field("min_sr", Double::class.java), 2).`as`("min_sr"),
                    round(field("sr", Double::class.java), 2).`as`("sr"),
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("location", String::class.java),
                    field("seriesdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte")
            )
        val results = cte.select().from("cte2")
            .where(field("min_sr").eq(field("sr")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()

        for (row in results) {
            // want only one but there may be multiple scores with the same value
            val bb = BowlingRatesDto(
                row.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                row.getValue("overs", String::class.java),
                row.getValue("balls", Int::class.java),
                row.getValue("maidens", Int::class.java),
                row.getValue("wickets", Int::class.java),
                row.getValue("runs", Int::class.java),
                row.getValue("sr", Double::class.java),
                row.getValue("location").toString(),
                row.getValue("seriesdate").toString()
            )
            bestBowling.add(bb)
        }

        return bestBowling
    }

    fun getBestBowlingEconRate(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
        oversLimit: Int = 0,
    ): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        var whereClause = BOWLINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)
        val cte = context
            .with("cte").`as`(
                select(
                    MATCHES.BALLSPEROVER,
                    BOWLINGDETAILS.WICKETS,
                    BOWLINGDETAILS.OVERS,
                    BOWLINGDETAILS.BALLS,
                    BOWLINGDETAILS.MAIDENS,
                    BOWLINGDETAILS.RUNS,
                    min((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS.cast(Float::class.java)) * MATCHES.BALLSPEROVER).over()
                        .`as`("min_er"),
                    ((BOWLINGDETAILS.RUNS / BOWLINGDETAILS.BALLS.cast(Float::class.java)) * MATCHES.BALLSPEROVER).`as`(
                        "er"
                    ),
                    PLAYERSMATCHES.FULLNAME,
                    PLAYERSMATCHES.SORTNAMEPART,
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
                    .and(BOWLINGDETAILS.BALLS.ne(0))
                    .and(whereClause)
                    .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                    .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc(), PLAYERSMATCHES.SORTNAMEPART)
            ).with("cte2").`as`(
                select(
                    field("ballsPerOver", Int::class.java),
                    field("wickets", Int::class.java),
                    field("overs", String::class.java),
                    field("maidens", Int::class.java),
                    field("runs", Int::class.java),
                    field("balls", Int::class.java),
                    round(field("min_er", Double::class.java), 2).`as`("min_er"),
                    round(field("er", Double::class.java), 2).`as`("er"),
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("location", String::class.java),
                    field("seriesdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte")
            )
        val results = cte.select().from("cte2")
            .where(field("min_er").eq(field("er")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()

        for (row in results) {
            // want only one but there may be multiple scores with the same value
            val bb = BowlingRatesDto(
                row.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                row.getValue("overs", String::class.java),
                row.getValue("balls", Int::class.java),
                row.getValue("maidens", Int::class.java),
                row.getValue("wickets", Int::class.java),
                row.getValue("runs", Int::class.java),
                row.getValue("er", Double::class.java),
                row.getValue("location").toString(),
                row.getValue("seriesdate").toString()
            )
            bestBowling.add(bb)
        }

        return bestBowling
    }

    fun getWorstBowlingEconRate(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long,
        oversLimit: Int = 0,
    ): List<BowlingRatesDto> {

        val bestBowling = mutableListOf<BowlingRatesDto>()

        var whereClause = BOWLINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)
        val cte = context
            .with("cte").`as`(
                select(
                    MATCHES.BALLSPEROVER,
                    BOWLINGDETAILS.WICKETS,
                    BOWLINGDETAILS.OVERS,
                    BOWLINGDETAILS.BALLS,
                    BOWLINGDETAILS.MAIDENS,
                    BOWLINGDETAILS.RUNS,
                    max(((BOWLINGDETAILS.RUNS).divide(BOWLINGDETAILS.BALLS.cast(Float::class.java))) * MATCHES.BALLSPEROVER).over()
                        .`as`("max_er"),
                    (((BOWLINGDETAILS.RUNS) / BOWLINGDETAILS.BALLS.cast(Float::class.java)) * MATCHES.BALLSPEROVER
                            ).`as`("er"),
                    PLAYERSMATCHES.FULLNAME,
                    PLAYERSMATCHES.SORTNAMEPART,
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
                    .and(BOWLINGDETAILS.BALLS.ne(0))
                    .and(whereClause)
                    .and(BOWLINGDETAILS.BALLS.div(MATCHES.BALLSPEROVER).greaterOrEqual(oversLimit))
                    .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc(), PLAYERSMATCHES.SORTNAMEPART)
            ).with("cte2").`as`(
                select(
                    field("ballsPerOver", Int::class.java),
                    field("wickets", Int::class.java),
                    field("overs", String::class.java),
                    field("maidens", Int::class.java),
                    field("runs", Int::class.java),
                    field("balls", Int::class.java),
                    round(field("max_er", Double::class.java), 2).`as`("max_er"),
                    round(field("er", Double::class.java), 2).`as`("er"),
                    field("fullname", String::class.java),
                    field("sortnamepart", String::class.java),
                    field("location", String::class.java),
                    field("seriesdate", String::class.java),
                    field("matchstartdateasoffset", String::class.java),
                ).from("cte")
            )
        val results = cte.select(
            field("ballsPerOver", Int::class.java),
            field("wickets", Int::class.java),
            field("overs", String::class.java),
            field("maidens", Int::class.java),
            field("runs", Int::class.java),
            field("balls", Int::class.java),
            field("er", Double::class.java),
            field("fullname", String::class.java),
            field("location", String::class.java),
            field("seriesdate", String::class.java),
            field("matchstartdateasoffset", String::class.java),
        ).from("cte2")
            .where(field("max_er").eq(field("er")))
            .orderBy(field("matchstartdateasoffset"), field("sortnamepart"), field("fullname"))
            .fetch()

        for (row in results) {
            // want only one but there may be multiple scores with the same value
            val bb = BowlingRatesDto(
                row.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                row.getValue("overs", String::class.java),
                row.getValue("balls", Int::class.java),
                row.getValue("maidens", Int::class.java),
                row.getValue("wickets", Int::class.java),
                row.getValue("runs", Int::class.java),
                row.getValue("er", Double::class.java),
                row.getValue("location").toString(),
                row.getValue("seriesdate").toString()
            )
            bestBowling.add(bb)
        }

        return bestBowling
    }

    fun getBestBowlingMatch(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): List<BestBowlingDto> {


        val bestBowling = mutableListOf<BestBowlingDto>()

        val t = TEAMS.`as`("t")
        val o = TEAMS.`as`("o")

        var whereClause = BOWLINGDETAILS.MATCHID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        try {
            val context = using(connection, dialect)

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
                    ).`as`("synbb"),
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
                    .and(whereClause)
                    .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
            ).with("cte2").`as`(
                select(
                    field("fullname"),
                    field("sortnamepart"),
                    field("seriesdate"),
                    field("matchstartdateasoffset"),
                    field("location"),
                    field("wickets"),
                    field("runs"),
                    field("synbb"),
                    max(field("synbb")).over().`as`("max_synbb")
                ).from("cte")
                    .where(field("synbb").isNotNull).and(field("rn").eq(1))
                    .orderBy(
                        field("matchstartdateasoffset"),
                        field("sortnamepart"),
                        field("fullname")
                    )
            )

            val query = q.select().from("cte2").where(field("synbb").eq(field("max_synbb")))

            val result = query.fetch()

            var previous = 0.0
            for (r in result) {

                val current = r.getValue("synbb", Double::class.java)
                // want only one but there may be multiple scores with the same value
                if (previous <= current) {
                    val bb = BestBowlingDto(
                        r.getValue("fullname", String::class.java),
                        teamParams.team,
                        teamParams.opponents,
                        r.getValue("wickets", Int::class.java),
                        r.getValue("runs", Int::class.java),
                        r.getValue("location").toString(),
                        r.getValue("seriesdate").toString()
                    )
                    bestBowling.add(bb)
                } else {
                    break
                }
                previous = current
            }
        } catch (e: Exception) {
            println(e.message)
            throw e
        }

        return bestBowling
    }

    fun getMostRuns(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): MutableList<MostRunsDto> {
        val mostruns = mutableListOf<MostRunsDto>()

        var whereClause = MATCHES.ID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        try {
            val context = using(connection, dialect)
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
                    count().filterWhere(BATTINGDETAILS.NOTOUT.eq(1)).over().partitionBy(BATTINGDETAILS.PLAYERID)
                        .`as`("notouts"),
                    max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("hs"),
                    rowNumber().over().partitionBy(BATTINGDETAILS.PLAYERID).`as`("rn")
                ).from(MATCHES)
                    .join(BATTINGDETAILS).on(BATTINGDETAILS.MATCHID.eq(MATCHES.ID))
                    .join(PLAYERS).on(PLAYERS.ID.eq(BATTINGDETAILS.PLAYERID))
                    .where(whereClause)
                    .and(BATTINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                    .and(BATTINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BATTINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))

            ).with("cte2").`as`(
                select(
                    field("fullname"),
                    field("sortnamepart"),
                    field("matches"),
                    field("runs"),
                    max(field("runs")).over().`as`("max_runs"),
                    field("innings"),
                    field("notouts"),
                    `when`(field("innings").sub(field("notouts")).eq(0), 0.0)
                        .otherwise(
                            trunc(
                                field("runs", Double::class.java)
                                    .divide(
                                        (field("innings", Int::class.java).subtract(
                                            field(
                                                "notouts",
                                                Int::class.java
                                            )
                                        )).cast(Float::class.java)
                                    ), 2
                            )
                        ).`as`("avg"),
                    field("hs"),
                ).from("cte")
                    .where(field("rn").eq(1))
            )

            val query = q.select()
                .from("cte2")
                .where(field("runs").eq(field("max_runs")))
                .orderBy(field("runs").desc(), field("sortnamepart"), field("fullname"))

            val result = query.fetch()

            for (r in result) {
                val mr = MostRunsDto(
                    r.getValue("fullname", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    r.getValue("matches", Int::class.java),
                    r.getValue("runs", Int::class.java),
                    r.getValue("innings", Int::class.java),
                    r.getValue("notouts", Int::class.java),
                    r.getValue("avg", Double::class.java),
                    getHighestscore(r.getValue("hs", Double::class.java))
                )
                mostruns.add(mr)
            }
        } catch (e: Exception) {
            println(e.message)
        }

        return mostruns
    }

    fun getMostWickets(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): MutableList<MostWicketsDto> {
        val mostwickets = mutableListOf<MostWicketsDto>()

        var whereClause = MATCHES.ID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)

        val t1Name = "getbestbowlingtemptable1"
        val tbbName = "getbestbowlingtemptablebb"
        val t3Name = "getbestbowlingtemptable3"

        context.createTemporaryTable(t1Name).`as`(

            select(
                PLAYERS.ID,
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
                .where(whereClause)
                .and(BOWLINGDETAILS.MATCHTYPE.notIn(internationalMatchTypes))
                .and(BOWLINGDETAILS.BALLS.notEqual(0))
                .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
        ).withData().execute()

        context.createTemporaryTable(tbbName).`as`(
            select(
                BOWLINGDETAILS.PLAYERID,
                BOWLINGDETAILS.RUNS,
                BOWLINGDETAILS.WICKETS,
                BOWLINGDETAILS.SYNTHETICBESTBOWLING,
                rowNumber().over().partitionBy(BOWLINGDETAILS.PLAYERID)
                    .orderBy(BOWLINGDETAILS.WICKETS.desc(), BOWLINGDETAILS.RUNS).`as`("rn")
            ).from(BOWLINGDETAILS)
                .join(MATCHES).on(BOWLINGDETAILS.MATCHID.eq(MATCHES.ID))
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
        ).withData().execute()

        context.createTemporaryTable(t3Name).`as`(
            select(
                field("fullname"),
                field("sortnamepart"),
                field("matches"),
                field("balls"),
                field("maidens"),
                field("$t1Name.runs"),
                field("$t1Name.wickets"),
                field("$t1Name.bb"),
                max(field("$t1Name.wickets")).over().`as`("max_wickets"),
                field("$tbbName.wickets").`as`("bbwickets"),
                field("$tbbName.runs").`as`("bbruns"),
                `when`(field("$t1Name.wickets").eq(0), 0.0)
                    .otherwise(
                        trunc(
                            field("$t1Name.runs", Double::class.java)
                                .divide(
                                    (field("$t1Name.wickets").cast(Float::class.java))
                                ), 2
                        )
                    ).`as`("avg")
            ).from(t1Name)
                .join(tbbName)
                .on(field("$tbbName.playerid", Int::class.java).eq(field("$t1Name.id", Int::class.java)))
                .and(
                    field("$tbbName.syntheticbestbowling", Double::class.java).eq(
                        field(
                            "$t1Name.bb",
                            Double::class.java
                        )
                    )
                )
                .where(field("$tbbName.rn").eq(1)).and(field("$t1Name.rn").eq(1))
        ).withData().execute()


        val queryTemp = context.select()
            .from(t3Name)
            .where(field("wickets").eq(field("max_wickets")))
            .orderBy(field("wickets").desc(), field("sortnamepart"), field("fullname"))

        val result = try {
            queryTemp.fetch()
        } finally {
            context.dropTable(t1Name).execute()
            context.dropTable(tbbName).execute()
            context.dropTable(t3Name).execute()
        }
        for (r in result) {
            val mr = MostWicketsDto(
                r.getValue("fullname", String::class.java),
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
            mostwickets.add(mr)
        }

        return mostwickets
    }

    fun getMostCatches(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): MutableList<MostDismissalsDto> {
        val mostCatches = mutableListOf<MostDismissalsDto>()

        var whereClause = MATCHES.ID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)

        val tmpTableName = "tmp_caught"
        context.createTemporaryTable(tmpTableName).`as`(
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
                .where(whereClause)
                .and(MATCHES.MATCHTYPE.notIn(internationalMatchTypes))
                .and(FIELDING.TEAMID.`in`(teamParams.teamIds))
                .and(FIELDING.OPPONENTSID.`in`(teamParams.opponentIds))
        ).withData().execute()

        val query = context.with("cte").`as`(
            select(
                field("rn"),
                field("fullname"),
                field("sortnamepart"),
                field("matches"),
                field("caught"),
                max(field("caught")).over().`as`("max_caught")
            ).from(tmpTableName)
        ).select()
            .from("cte")
            .where(field("rn").eq(1))
            .and(field("caught").gt(0))
            .and(field("caught").eq(field("max_caught")))
            .orderBy(field("caught").desc(), field("sortnamepart"), field("fullname"))

        val result = try {
            query.fetch()
        } finally {
            context.dropTable(tmpTableName).execute()
        }

        for (r in result) {
            val mr = MostDismissalsDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("matches", Int::class.java),
                r.getValue("caught", Int::class.java)
            )
            mostCatches.add(mr)
        }

        return mostCatches
    }

    fun getMostStumpings(
        countryIds: List<Int>,
        teamParams: TeamParams,
        startFrom: Long
    ): MutableList<MostDismissalsDto> {
        val mostStumpings = mutableListOf<MostDismissalsDto>()

        var whereClause = MATCHES.ID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val context = using(connection, dialect)

        val tmpTableName = "tmp_stumpings"
        context.createTemporaryTable(tmpTableName).`as`(
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
                .where(whereClause)
                .and(MATCHES.MATCHTYPE.notIn(internationalMatchTypes))
                .and(FIELDING.TEAMID.`in`(teamParams.teamIds))
                .and(FIELDING.OPPONENTSID.`in`(teamParams.opponentIds))
        ).withData().execute()

        val query = context.with("cte").`as`(
            select(
                field("rn"),
                field("fullname"),
                field("sortnamepart"),
                field("matches"),
                field("stumpings"),
                max(field("stumpings")).over().`as`("max_stumpings")
            ).from(tmpTableName)
        ).select()
            .from("cte")
            .where(field("rn").eq(1))
            .and(field("stumpings").gt(0))
            .and(field("stumpings").eq(field("max_stumpings")))
            .orderBy(field("stumpings").desc(), field("sortnamepart"), field("fullname"))

        val result = try {
            query.fetch()
        } finally {
            context.dropTable(tmpTableName).execute()
        }

        for (r in result) {
            val mr = MostDismissalsDto(
                r.getValue("fullname", String::class.java),
                teamParams.team,
                teamParams.opponents,
                r.getValue("matches", Int::class.java),
                r.getValue("stumpings", Int::class.java)
            )
            mostStumpings.add(mr)
        }

        return mostStumpings
    }

    private fun getHighestscore(value: Double?): String {
        if (value == null) return ""
        if (value.rem(1) == 0.0) return value.toInt().toString()
        return "${(value - 0.5).toInt()}*"
    }


    fun getHighestFoW(countryIds: List<Int>, teamParams: TeamParams, startFrom: Long): MutableMap<Int, FowDetails> {

        val bestFow = mutableMapOf<Int, FowDetails>()


        val context = using(connection, dialect)

        var whereClause = PARTNERSHIPS.matches.ID.`in`(
            select(MATCHSUBTYPE.MATCHID).from(
                MATCHSUBTYPE.where(
                    MATCHSUBTYPE.MATCHTYPE.eq(
                        teamParams.matchSubType
                    )
                )
            )
        ).and(MATCHES.MATCHSTARTDATEASOFFSET.gt(startFrom).or(MATCHES.MATCHSTARTDATE.isNull))

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val tmpTableName = "tmp_partnerships"
        context.createTemporaryTable(tmpTableName).`as`(
            select(
                PARTNERSHIPS.matches.ID,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.PARTNERSHIP,
                PARTNERSHIPS.UNBROKEN,
                PARTNERSHIPS.PLAYERNAMES,
                PARTNERSHIPS.matches.LOCATION,
                PARTNERSHIPS.matches.SERIESDATE,
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
                .where(PARTNERSHIPS.matches.MATCHTYPE.eq(teamParams.matchType))
                .and(whereClause)
                .and(PARTNERSHIPS.MATCHTYPE.notIn(internationalMatchTypes))
                .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
                .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
                .and(PARTNERSHIPS.MULTIPLE.eq(0))
                .orderBy(
                    PARTNERSHIPS.WICKET,
                    PARTNERSHIPS.PARTNERSHIP.desc(),
                    PARTNERSHIPS.UNBROKEN.desc(),
                    PARTNERSHIPS.matches.ID,
                    field("fullname2").desc(),
                    PLAYERSMATCHES.FULLNAME.desc(),
                )
        ).withData().execute()

        try {
            for (wicket in 1..10) {
                val listFoW = mutableListOf<FoWDto>()
                val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()

                val query = context
                    .with("cte").`as`(
                        select(
                            field("id"),
                            field("playerids"),
                            field("wicket"),
                            field("inningsorder"),
                            field("partnership"),
                            field("unbroken"),
                            field("playernames"),
                            field("location"),
                            field("seriesdate"),
                            field("matchstartdateasoffset"),
                            field("fullname"),
                            field("score"),
                            field("notout"),
                            field("position"),
                            field("fullname2"),
                            field("score2"),
                            field("notout2"),
                            field("position2"),
                            (field("partnership") + field("unbroken").cast(Float::class.java)
                                .div(10)).`as`("synth_partnership"),
                            max(field("partnership") + field("unbroken").cast(Float::class.java).div(10)).over()
                                .`as`("max_partnership"),
                        )
                            .from(tmpTableName)
                            .where(field("wicket").eq(wicket))
                            .and(field("rn").eq(1))
                    ).with("cte2").`as`(
                        select()
                            .from("cte")
                            .where(field("synth_partnership").eq(field("max_partnership")))
                    )

                val result = query.select()
                    .from("cte2")
                    .orderBy(
                        field("partnership").desc(),
                        field("matchstartdateasoffset"),
                        field("inningsorder")
                    )
                    .fetch()

                for (partnershipRecord in result) {

                    val partnership = partnershipRecord.getValue("partnership", Int::class.java)

                    val possileInvalidPartnership = getPossibleFallOfWicketMissingPartnerships(
                        connection,
                        dialect,
                        countryIds,
                        teamParams,
                        wicket,
                        partnership,
                        startFrom
                    )

                    // want only one but there may be multiple scores with the same value
                    val fow = FoWDto(
                        teamParams.team,
                        teamParams.opponents,
                        partnershipRecord.getValue("location", String::class.java),
                        partnershipRecord.getValue("seriesdate", String::class.java),
                        partnership,
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
                        possileInvalidPartnership
                    )

                    // only look for a multi-player fall-of-wicket if we haven't already added one for this wicket
                    // this stops the issue where I have identical partnerships for the FoW
                    // e.g. 2 partnerships of 154 for the first wicket
                    // as in that case I'd check the multi-player FoW twice
                    if (listMultiPlayerFowDao.filter { it.wicket == fow.wicket }.size == 0) {
                        listMultiPlayerFowDao.addAll(
                            getMultiplePlayerFow(
                                fow.wicket,
                                fow.partnership,
                                teamParams,
                                context
                            )
                        )
                    }

                    listFoW.add(fow)


                }
                bestFow[wicket] = FowDetails(listFoW, listMultiPlayerFowDao)

            }
        } finally {
            context.dropTable(tmpTableName).execute()
        }

        return bestFow
    }

    private fun getMultiplePlayerFow(
        wicket: Int,
        partnership: Int,
        teamParams: TeamParams,
        context: DSLContext,
    ): MutableList<MultiPlayerFowDto> {

        val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDto>()
        val possibleMatches = isMultiPlayerpartnership(partnership, wicket, teamParams, context)
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
                    select().from("cte")
                        .where(field("rn").eq(1))


                )
            val resultMultiples =
                queryMultiples.select().from("cte2")
                    .orderBy(
                        field("matchstartdateasoffset"),
                        field("multiple").desc(),
                        field("position"),
                        field("position2"),
                    )
                    .fetch()

            if (resultMultiples.size > 1) {
                val listMultiPlayerFow = mutableListOf<FoWDto>()

                val possibleBestpartnership = resultMultiples.get(0).getValue("Partnership", Int::class.java)

                if (possibleBestpartnership >= partnership) {
                    log.info(
                        "Have a possible match for multiple wicket record ${teamParams.team} v ${teamParams.opponents} for the ${
                            getWicket(
                                wicket
                            )
                        } wicket"
                    )

                    var total = 0
                    var unbroken = false

                    for ((index, rmultiple) in resultMultiples.withIndex()) {

                        // the first entry is the total partnership
                        if (index == 0) {
                            total = rmultiple.getValue("Partnership", Int::class.java)
                            unbroken = rmultiple.getValue("Unbroken", Boolean::class.java)
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
                            rmultiple.getValue("Position", Int::class.java),
                            rmultiple.getValue("fullname2", String::class.java) ?: "unknown",
                            rmultiple.getValue("score2", Int::class.java),
                            rmultiple.getValue("notout2", Boolean::class.java),
                            rmultiple.getValue("position2", Int::class.java),
                        )

                        listMultiPlayerFow.add(multiPlayerFow)
                    }

                    listMultiPlayerFowDao.add(MultiPlayerFowDto(total, unbroken, wicket, listMultiPlayerFow))
                }
            }
        }
        return listMultiPlayerFowDao
    }

    private fun isMultiPlayerpartnership(
        partnership: Int,
        wicket: Int,
        teamParams: TeamParams,
        context: DSLContext,
    ): List<PossibleMultiPlayerPartnerships> {

        val possibleMatches = mutableListOf<PossibleMultiPlayerPartnerships>()

        val result = context.select(PARTNERSHIPS.MATCHID, PARTNERSHIPS.INNINGSORDER)
            .from(PARTNERSHIPS)
            .join(MATCHES).on(PARTNERSHIPS.MATCHID.eq(MATCHES.ID))
            .where(PARTNERSHIPS.WICKET.eq(wicket))
            .and(PARTNERSHIPS.PARTNERSHIP.ge(partnership))
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            .and(PARTNERSHIPS.MATCHTYPE.eq(teamParams.matchType))
            .and(PARTNERSHIPS.MULTIPLE.eq(1))
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

    private fun getMultiPlayerPartnershipForGivenWicket(
        matchId: Int,
        wicket: Int,
        teamParams: TeamParams,
        inningsOrder: Int,
    ): SelectConditionStep<Record16<Int, String?, String?, Long?, Int?, Int?, Int?, Byte?, String?, Int?, Byte?, Int?, String?, Int?, Byte?, Int?>> {

        return select(
            rowNumber().over()
                .partitionBy(
                    PARTNERSHIPS.MATCHID,
                    PARTNERSHIPS.TEAMID,
                    PARTNERSHIPS.INNINGSORDER,
                    PARTNERSHIPS.WICKET,
                    PARTNERSHIPS.PLAYERIDS,
                    PARTNERSHIPS.MULTIPLE
                ).orderBy(
                    PARTNERSHIPS.MATCHID,
                    PARTNERSHIPS.TEAMID,
                    PARTNERSHIPS.INNINGSORDER,
                    PARTNERSHIPS.WICKET,
                    PARTNERSHIPS.PLAYERIDS,
                    PARTNERSHIPS.MULTIPLE
                ).`as`("rn"),
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
            BATTINGDETAILS.POSITION,
            lead(PLAYERSMATCHES.FULLNAME).over().partitionBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).orderBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).`as`("fullname2"),
            lead(BATTINGDETAILS.SCORE).over().partitionBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).orderBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).`as`("score2"),
            lead(BATTINGDETAILS.NOTOUT).over().partitionBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).orderBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).`as`("notout2"),
            lead(BATTINGDETAILS.POSITION).over().partitionBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).orderBy(
                PARTNERSHIPS.MATCHID,
                PARTNERSHIPS.TEAMID,
                PARTNERSHIPS.INNINGSORDER,
                PARTNERSHIPS.WICKET,
                PARTNERSHIPS.PLAYERIDS,
                PARTNERSHIPS.MULTIPLE
            ).`as`("position2"),
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
    }
}




