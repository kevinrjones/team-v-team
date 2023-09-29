package com.knowledgespike.teamvteam.database

import com.knowledgespike.db.tables.references.*
import com.knowledgespike.teamvteam.daos.*
import com.knowledgespike.teamvteam.helpers.getWicket
import com.knowledgespike.teamvteam.logging.LoggerDelegate
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import java.sql.DriverManager


class TeamRecords(val userName: String, val password: String, val connectionString: String) {

    val log by LoggerDelegate()

    fun getHighestTotals(
        teamParams: TeamParams,
    ): List<TotalDao> {
        val highestTotals = mutableListOf<TotalDao>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)
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
                        INNINGS.matches.SERIESDATE
                    )
                        .from(INNINGS)
                        .where(INNINGS.matches.MATCHTYPE.eq(teamParams.matchType))
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
                        .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                        .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
                )

            val result = cte.select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
            ).from("cte").where(field("max_synth").eq(field("synth")))
                .fetch()

            var previous = 0
            for (r in result) {
                val hs = TotalDao(
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

    fun getLowestTotals(
        teamParams: TeamParams,
        limit: Int = 5
    ): List<TotalDao> {
        val lowestTotals = mutableListOf<TotalDao>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)
            val result = context.with("cte").`as`(
                select(
                    INNINGS.TOTAL,
                    min(INNINGS.TOTAL).over().`as`("min_total"),
                    INNINGS.WICKETS,
                    INNINGS.DECLARED,
                    INNINGS.matches.LOCATION,
                    INNINGS.matches.SERIESDATE
                )
                    .from(INNINGS)
                    .where(INNINGS.matches.MATCHTYPE.eq(teamParams.matchType))
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
                    .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                    .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(INNINGS.COMPLETE)
            ).select(
                field("Total", Int::class.java),
                field("Wickets", Int::class.java),
                field("Declared", Boolean::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
            ).from("cte").where(field("min_total").eq(field("total")))
                .fetch()

            for (r in result) {
                val hs = TotalDao(
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

    fun getHighestIndividualScores(teamParams: TeamParams): List<HighestScoreDao> {
        val highestScores = mutableListOf<HighestScoreDao>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)
            val result = context
                .with("cte").`as`(
                    select(
                        BATTINGDETAILS.FULLNAME,
                        BATTINGDETAILS.SCORE,
                        max(BATTINGDETAILS.NOTOUTADJUSTEDSCORE).over().`as`("max_score"),
                        BATTINGDETAILS.NOTOUT,
                        BATTINGDETAILS.NOTOUTADJUSTEDSCORE,
                        BATTINGDETAILS.matches.LOCATION,
                        BATTINGDETAILS.matches.SERIESDATE
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
                        .orderBy(BATTINGDETAILS.NOTOUTADJUSTEDSCORE.desc())
                        .limit(teamParams.limit)
                ).select(
                    field("FullName", String::class.java),
                    field("Score", Int::class.java),
                    field("NotOut", Boolean::class.java),
                    field("Location", String::class.java),
                    field("SeriesDate", String::class.java),
                ).from("cte").where(field("max_score").eq(field("NotOutAdjustedScore")))
                .fetch()



            for (r in result) {
                // want only one but there may be multiple scores with the same value
                val hs = HighestScoreDao(
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

    fun getBestBowlingInnings(teamParams: TeamParams): List<BestBowlingDao> {

        val bestBowling = mutableListOf<BestBowlingDao>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)
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
                        .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                        .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
                        .orderBy(BOWLINGDETAILS.SYNTHETICBESTBOWLING.desc())
                        .limit(teamParams.limit)
                )
            val results = cte.select(
                field("Wickets", Int::class.java),
                field("Runs", Int::class.java),
                field("SyntheticBestBowling", Double::class.java),
                field("FullName", String::class.java),
                field("Location", String::class.java),
                field("SeriesDate", String::class.java),
            ).from("cte")
                .where(field("max_bb").eq(field("syntheticbestbowling")))
                .fetch()

            for (row in results) {
                val current = row.getValue("SyntheticBestBowling", Double::class.java)
                // want only one but there may be multiple scores with the same value
                val hs = BestBowlingDao(
                    row.getValue("FullName", String::class.java),
                    teamParams.team,
                    teamParams.opponents,
                    row.getValue("Wickets", Int::class.java),
                    row.getValue("Runs", Int::class.java),
                    row.getValue("Location").toString(),
                    row.getValue("SeriesDate").toString()
                )
                bestBowling.add(hs)
            }
        }
        return bestBowling
    }

    fun getBestBowlingMatch(teamParams: TeamParams): List<BestBowlingDao> {


        val bestBowling = mutableListOf<BestBowlingDao>()

        val t = TEAMS.`as`("t")
        val o = TEAMS.`as`("o")

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)

            val q = context.with(
                "cte"
            ).`as`(
                select(
                    PLAYERS.FULLNAME,
                    PLAYERS.SORTNAMEPART,
                    BOWLINGDETAILS.NAME,
                    MATCHES.SERIESDATE,
                    MATCHES.LOCATION,
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
                    .join(PLAYERS).on(PLAYERS.ID.eq(BOWLINGDETAILS.PLAYERID))
                    .join(t).on(t.ID.eq(BOWLINGDETAILS.TEAMID))
                    .join(o).on(o.ID.eq(BOWLINGDETAILS.OPPONENTSID))
                    .join(MATCHES).on(MATCHES.ID.eq(BOWLINGDETAILS.MATCHID))
                    .where(BOWLINGDETAILS.MATCHTYPE.eq(teamParams.matchType))
                    .and(BOWLINGDETAILS.TEAMID.`in`(teamParams.teamIds))
                    .and(BOWLINGDETAILS.OPPONENTSID.`in`(teamParams.opponentIds))
            ).with("cte2").`as`(
                select(
                    field("FullName"),
                    field("SeriesDate"),
                    field("Location"),
                    field("Wickets"),
                    field("Runs"),
                    field("synbb"),
                    max(field("synbb")).over().`as`("max_synbb")
                ).from("cte")
                    .where(field("synbb").isNotNull).and(field("rn").eq(1))
            )

            val query = q.select().from("cte2").where(field("synbb").eq(field("max_synbb")))

            val result = query.fetch()

            var previous = 0.0
            for (r in result) {

                val current = r.getValue("synbb", Double::class.java)
                // want only one but there may be multiple scores with the same value
                if (previous <= current) {
                    val hs = BestBowlingDao(
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

    fun getHighestFoW(teamParams: TeamParams): MutableMap<Int, FowDetails> {

        val bestFow = mutableMapOf<Int, FowDetails>()

        DriverManager.getConnection(connectionString, userName, password).use { conn ->
            val context = DSL.using(conn, SQLDialect.MYSQL)

            for (wicket in 1..10) {
                val listFoW = mutableListOf<FoWDao>()
                val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDao>()
                val selectFoW = createFowSelectForGivenWicketAndTeams(teamParams, wicket)


                val query = context
                    .with("cte").`as`(
                        selectFoW
                    ).with("cte2").`as`(
                        select().from("cte").where(field("synth_partnership").eq(field("max_partnership")))
                            .and(field("rn").eq(1))
                    )
                val result = query.select().from("cte2").fetch()


                var previousPartnership = 0.0

                for (partnershipRecord in result) {

                    // want only one but there may be multiple scores with the same value
                    val fow = FoWDao(
                        teamParams.team,
                        teamParams.opponents,
                        partnershipRecord.getValue("Location", String::class.java),
                        partnershipRecord.getValue("SeriesDate", String::class.java),
                        partnershipRecord.getValue("Partnership", Int::class.java),
                        partnershipRecord.getValue("Wicket", Int::class.java),
                        partnershipRecord.getValue("Unbroken", Boolean::class.java),
                        partnershipRecord.getValue("FullName", String::class.java),
                        partnershipRecord.getValue("Score", Int::class.java),
                        partnershipRecord.getValue("NotOut", Boolean::class.java),
                        partnershipRecord.getValue("fullName2", String::class.java),
                        partnershipRecord.getValue("score2", Int::class.java),
                        partnershipRecord.getValue("notout2", Boolean::class.java),
                    )

                    listMultiPlayerFowDao.addAll(
                        getMultiplePlayerFow(
                            teamParams.matchType,
                            partnershipRecord,
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
        matchType: String,
        fowRecord: Record,
        wicket: Int,
        partnership: Int,
        teamParams: TeamParams,
        context: DSLContext

    ): MutableList<MultiPlayerFowDao> {

        val listMultiPlayerFowDao = mutableListOf<MultiPlayerFowDao>()
        val possibleMatches = isMultiPlayerPartnership(partnership, wicket, teamParams, context)
        possibleMatches.forEach {match ->
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

                )
            val resultMultiples = queryMultiples.select().from("cte2")
                .fetch()

            if (resultMultiples.size > 1) {
                val listMultiPlayerFow = mutableListOf<FoWDao>()

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

                        val multiPlayerFow = FoWDao(
                            teamParams.team,
                            teamParams.opponents,
                            rmultiple.getValue("Location", String::class.java),
                            rmultiple.getValue("SeriesDate", String::class.java),
                            rmultiple.getValue("Partnership", Int::class.java),
                            rmultiple.getValue("Wicket", Int::class.java),
                            rmultiple.getValue("Unbroken", Boolean::class.java),
                            rmultiple.getValue("FullName", String::class.java),
                            rmultiple.getValue("Score", Int::class.java),
                            rmultiple.getValue("NotOut", Boolean::class.java),
                            rmultiple.getValue("fullName2", String::class.java),
                            rmultiple.getValue("score2", Int::class.java),
                            rmultiple.getValue("notout2", Boolean::class.java),
                        )

                        listMultiPlayerFow.add(multiPlayerFow)
                    }

                    listMultiPlayerFowDao.add(MultiPlayerFowDao(total, wicket, listMultiPlayerFow))
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
    ): SelectSeekStep3<Record17<Int?, Int?, Int?, Int?, Boolean?, String?, String?, String?, Int?, Int?, Int, String?, Int?, Boolean?, String?, Int?, Boolean?>, Int?, Boolean?, Int?> {

        return select(
            MATCHES.ID,
            PARTNERSHIPS.WICKET,
            PARTNERSHIPS.INNINGSORDER,
            PARTNERSHIPS.PARTNERSHIP,
            PARTNERSHIPS.UNBROKEN,
            PARTNERSHIPS.PLAYERNAMES,
            MATCHES.LOCATION,
            MATCHES.SERIESDATE,
            (PARTNERSHIPS.PARTNERSHIP + PARTNERSHIPS.UNBROKEN.div(10)).`as`("synth_partnership"),
            max(PARTNERSHIPS.PARTNERSHIP + PARTNERSHIPS.UNBROKEN.div(10)).over().`as`("max_partnership"),
            rowNumber().over().partitionBy(MATCHES.ID, PARTNERSHIPS.PARTNERSHIP).`as`("rn"),
            PLAYERS.FULLNAME,
            BATTINGDETAILS.SCORE,
            BATTINGDETAILS.NOTOUT,
            lead(PLAYERS.FULLNAME).over().partitionBy(MATCHES.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("fullName2"),
            lead(BATTINGDETAILS.SCORE).over().partitionBy(MATCHES.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("score2"),
            lead(BATTINGDETAILS.NOTOUT).over().partitionBy(MATCHES.ID, PARTNERSHIPS.PARTNERSHIP)
                .`as`("notout2"),
        )
            .from(PARTNERSHIPS)
            .join(MATCHES).on(MATCHES.ID.eq(PARTNERSHIPS.MATCHID))
            .leftOuterJoin(PARTNERSHIPSPLAYERS).on(PARTNERSHIPSPLAYERS.PARTNERSHIPID.eq(PARTNERSHIPS.ID))
            .leftOuterJoin(PLAYERS).on(PLAYERS.ID.eq(PARTNERSHIPSPLAYERS.PLAYERID))
            .leftOuterJoin(BATTINGDETAILS).on(
                BATTINGDETAILS.MATCHID.eq(MATCHES.ID).and(
                    BATTINGDETAILS.PLAYERID.eq(
                        PARTNERSHIPSPLAYERS.PLAYERID
                    ).and(BATTINGDETAILS.INNINGSORDER.eq(PARTNERSHIPS.INNINGSORDER))
                )
            )
            .where(MATCHES.MATCHTYPE.eq(teamParams.matchType))
            .and(
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
            .and(PARTNERSHIPS.TEAMID.`in`(teamParams.teamIds))
            .and(PARTNERSHIPS.OPPONENTSID.`in`(teamParams.opponentIds))
            .and(PARTNERSHIPS.MULTIPLE.eq(false))
            .and(PARTNERSHIPS.WICKET.eq(wicket))
            .orderBy(PARTNERSHIPS.PARTNERSHIP.desc(), PARTNERSHIPS.UNBROKEN.desc(), MATCHES.ID)
    }

    private fun getMultiPlayerPartnershipForGivenWicket(
        matchId: Int,
        wicket: Int,
        teamParams: TeamParams,
        inningsOrder: Int
    ): SelectSeekStep2<Record13<Int, String?, String?, Int?, Int?, Boolean?, Boolean?, String?, Int?, Boolean?, String?, Int?, Boolean?>, Int?, Boolean?> {
        return select(
            rowNumber().over().partitionBy(PARTNERSHIPS.PLAYERIDS).`as`("rn"),
            MATCHES.LOCATION,
            MATCHES.SERIESDATE,
            PARTNERSHIPS.PARTNERSHIP,
            PARTNERSHIPS.WICKET,
            PARTNERSHIPS.UNBROKEN,
            PARTNERSHIPS.MULTIPLE,
            PLAYERS.FULLNAME,
            BATTINGDETAILS.SCORE,
            BATTINGDETAILS.NOTOUT,
            lead(PLAYERS.FULLNAME).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("fullName2"),
            lead(BATTINGDETAILS.SCORE).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("score2"),
            lead(BATTINGDETAILS.NOTOUT).over().partitionBy(PARTNERSHIPS.PARTNERSHIP).`as`("notout2"),
        ).from(PARTNERSHIPS)
            .join(MATCHES).on(MATCHES.ID.eq(PARTNERSHIPS.MATCHID))
            .join(PARTNERSHIPSPLAYERS).on(PARTNERSHIPS.ID.eq(PARTNERSHIPSPLAYERS.PARTNERSHIPID))
            .leftOuterJoin(PLAYERS).on(PLAYERS.ID.eq(PARTNERSHIPSPLAYERS.PLAYERID))
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

    private fun executeGetTotals(
        query: SelectConditionStep<Record5<Int?, Int?, Boolean?, String?, String?>>,
        isHighest: Boolean,
        limit: Int = 5,
        block: (Result<Record5<Int?, Int?, Boolean?, String?, String?>>) -> Unit
    ) {

        val result: Result<Record5<Int?, Int?, Boolean?, String?, String?>> = if (isHighest) {
            query
                .orderBy(INNINGS.TOTAL.desc())
                .limit(limit)
                .fetch()
        } else {
            query
                .orderBy(INNINGS.TOTAL.asc())
                .limit(limit)
                .fetch()
        }
        block(result)
    }

}

data class TeamParams(
    val teamIds: List<Int>,
    val opponentIds: List<Int>,
    val team: String,
    val opponents: String,
    val matchType: String,
    val matchSubType: String,
    val limit: Int = 5
)



