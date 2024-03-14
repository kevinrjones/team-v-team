package com.knowledgespike.shared.data

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile


fun getAllCompetitions(dataDirectory: String): List<Competition> {

    val allCompetitions = mutableListOf<Competition>()
    Files.list(Paths.get(dataDirectory))
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
    return allCompetitions
}

