package com.silwek.grenadine.datasource.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.silwek.grenadine.models.Note
import java.time.LocalDateTime

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "label") var label: String?,
    @ColumnInfo(name = "creationDate") var creationDate: LocalDateTime?,
    @ColumnInfo(name = "staleDate") var staleDate: LocalDateTime?
) {
    fun toNote(): Note {
        return Note(
            label = label,
            id = uid.toString(),
            creationDate = LocalDateTime.from(creationDate),
            staleDate = LocalDateTime.from(staleDate)
        ).apply {
            computeStaleProgress()
        }
    }

    companion object {
        fun fromNote(note: Note): NoteEntity {
            return with(note) {
                NoteEntity(
                    uid = id?.toInt() ?: 0,
                    label = label,
                    creationDate = LocalDateTime.from(creationDate),
                    staleDate = LocalDateTime.from(staleDate)
                )
            }
        }
    }
}