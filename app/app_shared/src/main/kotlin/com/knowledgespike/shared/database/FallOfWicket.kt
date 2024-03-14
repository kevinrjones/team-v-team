package com.knowledgespike.shared.database

import com.knowledgespike.db.tables.references.FALLOFWICKETS
import com.knowledgespike.db.tables.references.INNINGS
import com.knowledgespike.db.tables.references.MATCHES
import com.knowledgespike.db.tables.references.MATCHSUBTYPE
import com.knowledgespike.shared.data.TeamParams
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import java.sql.DriverManager

fun getPossibleFallOfWicketMissingPartnerships(
    databaseConnection: DatabaseConnection,
    countryIds: List<Int>,
    teamParams: TeamParams,
    wicket: Int,
    partnership: Int
): Boolean {
    DriverManager.getConnection(
        databaseConnection.connectionString,
        databaseConnection.userName,
        databaseConnection.password
    ).use { conn ->
        val context = DSL.using(conn, databaseConnection.dialect)

        var whereClause = MATCHES.MATCHTYPE.eq(teamParams.matchType).and(
            MATCHES.ID.`in`(
                select(MATCHSUBTYPE.MATCHID).from(
                    MATCHSUBTYPE.where(
                        MATCHSUBTYPE.MATCHTYPE.eq(
                            teamParams.matchSubType
                        ).and(MATCHES.VICTORYTYPE.ne(11))
                            .and(
                                (MATCHES.HOMETEAMID.`in`(teamParams.teamIds)
                                    .and(MATCHES.AWAYTEAMID.`in`(teamParams.opponentIds)))
                                    .or(
                                        (MATCHES.AWAYTEAMID.`in`(teamParams.teamIds)
                                            .and(MATCHES.HOMETEAMID.`in`(teamParams.opponentIds)))
                                    )
                            )
                    )
                )
            )
        )

        if (countryIds.isNotEmpty())
            whereClause = whereClause.and(MATCHES.HOMECOUNTRYID.`in`(countryIds))

        val query = context
            .with("cte").`as`(
                select(
                    field("id"),
                ).from(MATCHES)
                    .where(whereClause)
            ).with("cte1")
            .`as`(
                select()
                    .from(FALLOFWICKETS)
                    .where(
                        FALLOFWICKETS.MATCHID.`in`(
                            select(
                                field("id", Int::class.java)
                            ).from("cte")
                        )
                    ).and(
                        FALLOFWICKETS.WICKET.eq(wicket)
                    )
                    .and(FALLOFWICKETS.TEAMID.`in`(teamParams.teamIds))
                    .and(FALLOFWICKETS.OPPONENTSID.`in`(teamParams.opponentIds))
                    .and(FALLOFWICKETS.CURRENTSCORE.isNull)
            ).with("cte2").`as`(
                select(INNINGS.MATCHID, INNINGS.TOTAL.`as`("total"))
                    .from(INNINGS)
                    .where(INNINGS.MATCHID.`in`(select(field("matchid", Int::class.java)).from("cte1")))
                    .and(INNINGS.TEAMID.`in`(teamParams.teamIds))
                    .and(INNINGS.OPPONENTSID.`in`(teamParams.opponentIds))
            )
            .select(count().`as`("count")).from(FALLOFWICKETS)
            .join("cte2")
            .on(FALLOFWICKETS.MATCHID.eq(field("cte2.matchid", Int::class.java)))
            .where(FALLOFWICKETS.MATCHID.`in`(select(field("matchid", Int::class.java)).from("cte2")))
            .and(FALLOFWICKETS.TEAMID.`in`(teamParams.teamIds))
            .and(FALLOFWICKETS.OPPONENTSID.`in`(teamParams.opponentIds))
            .and(FALLOFWICKETS.WICKET.ge(wicket))
            .and((FALLOFWICKETS.CURRENTSCORE.ge(partnership)).or(field("total").ge(partnership)))


        val res = query.fetch()
        val numberOfInvalidPartnerships = res.getValue(0, "count") as Int
        return numberOfInvalidPartnerships != 0
    }
}

