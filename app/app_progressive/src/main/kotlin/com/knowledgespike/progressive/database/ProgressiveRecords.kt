package com.knowledgespike.progressive.database

import com.knowledgespike.shared.database.DatabaseConnection
import com.knowledgespike.shared.logging.LoggerDelegate

class ProgressiveRecords(private val databaseConnection: DatabaseConnection) {

    private val log by LoggerDelegate()
}