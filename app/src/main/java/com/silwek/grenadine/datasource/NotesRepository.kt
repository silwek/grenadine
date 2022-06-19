package com.silwek.grenadine.datasource

import android.content.Context
import androidx.room.Room
import com.silwek.grenadine.datasource.room.RoomAppDatabase
import com.silwek.grenadine.datasource.room.entities.NoteEntity
import com.silwek.grenadine.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

interface NotesRepository {

    suspend fun loadNotes(onSuccess: (List<Note>) -> Unit)

    suspend fun addNote(note: Note, onSuccess: (id: String) -> Unit)

    suspend fun updateNote(newLabel: String, note: Note, onSuccess: (Note) -> Unit)

    suspend fun reviveNote(note: Note, newStaleDate: LocalDateTime, onSuccess: (Note) -> Unit)

    suspend fun removeNote(note: Note, onSuccess: () -> Unit)

    suspend fun removeNotes(notes: List<Note>, onSuccess: () -> Unit)
}


class RoomRepository(context: Context) : NotesRepository {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        RoomAppDatabase::class.java, "grenadine-name"
    ).build()
    private val noteDao by lazy { db.noteDao() }


    override suspend fun loadNotes(onSuccess: (List<Note>) -> Unit) {
        withContext(Dispatchers.IO) {
            val noteEntities = noteDao.getAll()
            val notes = noteEntities.map { it.toNote() }
            withContext(Dispatchers.Main) {
                onSuccess(notes)
            }
        }
    }

    override suspend fun addNote(note: Note, onSuccess: (id: String) -> Unit) {
        withContext(Dispatchers.IO) {
            val insertId = noteDao.insert(NoteEntity.fromNote(note))
            withContext(Dispatchers.Main) {
                onSuccess(insertId.toString())
            }
        }
    }

    override suspend fun updateNote(newLabel: String, note: Note, onSuccess: (Note) -> Unit) {
        withContext(Dispatchers.IO) {
            val newNote = NoteEntity.fromNote(note)
            newNote.label = newLabel
            noteDao.update(newNote)
            withContext(Dispatchers.Main) {
                onSuccess(newNote.toNote())
            }
        }
    }

    override suspend fun reviveNote(
        note: Note,
        newStaleDate: LocalDateTime,
        onSuccess: (Note) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val newNote = NoteEntity.fromNote(note)
            newNote.staleDate = newStaleDate
            noteDao.update(newNote)
            withContext(Dispatchers.Main) {
                onSuccess(newNote.toNote())
            }
        }
    }

    override suspend fun removeNote(note: Note, onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            noteDao.delete(NoteEntity.fromNote(note))
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    override suspend fun removeNotes(notes: List<Note>, onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            val noteEntities = notes.map { NoteEntity.fromNote(it) }
            noteDao.deleteNotes(*noteEntities.toTypedArray())
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    companion object {
        private const val TAG = "NotesRepository"
    }
}