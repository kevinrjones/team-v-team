package com.knowledgespike.shared.database

import org.jooq.SQLDialect

data class Connection(val userName: String, val password: String, val connectionString: String, val dialect: SQLDialect)