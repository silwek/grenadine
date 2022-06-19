package com.silwek.grenadine.models

import java.time.*

fun Long.secondToLocalDateTime(): LocalDateTime {
    return Instant.ofEpochSecond(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun LocalDateTime.toSecond(): Long {
    val zdt: ZonedDateTime = atZone(ZoneId.systemDefault())
    return zdt.toInstant().toEpochMilli() / 1000
}