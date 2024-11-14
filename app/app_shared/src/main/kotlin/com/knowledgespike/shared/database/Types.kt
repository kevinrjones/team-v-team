package com.knowledgespike.shared.database

import org.jooq.SQLDialect

data class DatabaseConnectionDetails(val userName: String, val password: String, val connectionString: String, val dialect: SQLDialect)