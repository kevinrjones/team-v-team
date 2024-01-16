package com.knowledgespike.shared.database

import com.knowledgespike.db.tables.references.MATCHES
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

fun checkIfShouldProcess(
    connectionString: String,
    userName: String,
    password: String,
    teamIds: List<Int>,
    opponentIds: List<Int>,
    matchType: String,
    dateOffset: Long,
    dialect: SQLDialect
): Boolean {
    DriverManager.getConnection(connectionString, userName, password).use { connection ->
        val context = DSL.using(connection, dialect)
        val res = context.select(MATCHES.ID).from(MATCHES).where(
            (MATCHES.HOMETEAMID.`in`(teamIds)
                .or(MATCHES.HOMETEAMID.`in`(opponentIds)))
                .and(
                    MATCHES.AWAYTEAMID.`in`(opponentIds)
                        .or(MATCHES.AWAYTEAMID.`in`(teamIds))
                )
                .and(MATCHES.MATCHTYPE.eq(matchType))
                .and(MATCHES.ADDEDDATEASOFFSET.gt(dateOffset))
        ).fetch()

        if (res.isNotEmpty) return true
    }
    return false
}

