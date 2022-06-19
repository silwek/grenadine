package com.silwek.grenadine.models

import java.time.Duration
import java.time.LocalDateTime

class Note(
    var label: String? = null,
    var creationDate: LocalDateTime? = null,
    var staleDate: LocalDateTime? = null,
    var id: String? = null
) {
    var staleProgress: Float = 0f
        private set


    fun computeStaleProgress() {
        val totalLifeTime =
            Duration.between(creationDate, staleDate).seconds.toFloat()
        val currentLifeTime =
            Duration.between(creationDate, LocalDateTime.now()).seconds.toFloat()
        staleProgress = currentLifeTime / totalLifeTime
    }

    fun isStaled(): Boolean {
        return staleProgress >= 1f
    }
}


