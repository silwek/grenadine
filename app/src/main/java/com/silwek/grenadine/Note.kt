package com.silwek.grenadine

import com.google.firebase.Timestamp
import java.time.Duration
import java.time.LocalDateTime

class Note(
    var label: String? = null,
    var creationDate: Timestamp? = null,
    var staleDate: Timestamp? = null,
    var id: String? = null
) {
    val creationLocalDateTime: LocalDateTime?
        get() {
            return creationDate?.toLocalDateTime()
        }
    val staleLocalDateTime: LocalDateTime?
        get() {
            return staleDate?.toLocalDateTime()
        }
    var staleProgress: Float = 0f
        private set


    fun computeStaleProgress() {
        val totalLifeTime =
            Duration.between(creationLocalDateTime, staleLocalDateTime).seconds.toFloat()
        val currentLifeTime =
            Duration.between(creationLocalDateTime, LocalDateTime.now()).seconds.toFloat()
        staleProgress = currentLifeTime / totalLifeTime
    }

    fun isStaled(): Boolean {
        return staleProgress >= 1f
    }
}


