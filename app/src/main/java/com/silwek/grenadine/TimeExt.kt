package com.silwek.grenadine

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochSecond(this.seconds)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}