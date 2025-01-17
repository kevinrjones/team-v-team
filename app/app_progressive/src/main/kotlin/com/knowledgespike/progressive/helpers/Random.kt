package com.knowledgespike.progressive.helpers

import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

const val STRING_LENGTH = 10

val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomString() = ThreadLocalRandom.current()
    .ints(STRING_LENGTH.toLong(), 0, charPool.size)
    .asSequence()
    .map(charPool::get)
    .joinToString("")