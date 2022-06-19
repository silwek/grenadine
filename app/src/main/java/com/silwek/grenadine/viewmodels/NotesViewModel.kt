package com.silwek.grenadine.viewmodels

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.silwek.grenadine.datasource.NotesRepository
import com.silwek.grenadine.models.Note
import java.util.*

class NotesViewModel : ViewModel() {
    private val repository = NotesRepository()
    private val _notes: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val notes: LiveData<MutableList<Note>> = _notes
    var editNote: Note? = null

    fun loadNotes() {
        repository.loadNotes { notes ->
            computeAllStaleProgress(notes)
            val finalList = removeStaledNotes(notes).toMutableList()
            Handler(Looper.getMainLooper()).run {
                _notes.value = finalList
            }
        }
    }

    fun addNote(label: String) {
        val note = Note(
            label = label,
            creationDate = Timestamp(Date()),
            staleDate = Timestamp(Date().toInstant().epochSecond + STALE_SEC_DEFAULT, 0)
        )
        repository.addNote(note) { id ->
            val notes = this.notes.value?.toMutableList() ?: ArrayList(1)
            note.id = id
            note.computeStaleProgress()
            notes.add(0, note)
            Handler(Looper.getMainLooper()).run {
                _notes.value = notes
            }
        }

    }

    fun removeNote(idToDelete: String) {
        repository.removeNote(idToDelete) {
            Handler(Looper.getMainLooper()).run {
                _notes.value = _notes.value?.apply { removeIf { it.id == idToDelete } }
            }
        }
    }

    private fun removeNotes(notes: List<Note>) {
        repository.removeNotes(notes) {
            Log.d(TAG, "Staled notes successfully deleted!")
        }
    }

    private fun computeAllStaleProgress(notes: List<Note>) {
        notes.forEach { it.computeStaleProgress() }
    }

    private fun removeStaledNotes(notes: List<Note>): List<Note> {
        val freshNotes = ArrayList<Note>()
        val staledNotes = ArrayList<Note>()
        notes.forEach {
            if (it.isStaled()) {
                staledNotes.add(it)
            } else {
                freshNotes.add(it)
            }
        }
        removeNotes(staledNotes)
        return orderNotes(freshNotes)
    }

    private fun orderNotes(notes: MutableList<Note>): List<Note> {
        return notes.sortedBy { it.staleProgress }
    }

    fun updateNote(newLabel: String, note: Note) {
        repository.updateNote(newLabel, note) {
            note.label = newLabel
            Handler(Looper.getMainLooper()).run {
                _notes.value = notes.value
            }
        }
    }

    fun reviveNote(note: Note) {
        val actualStaleDateSeconds = note.staleDate?.seconds ?: return
        val newStaleDate = Timestamp(actualStaleDateSeconds + STALE_SEC_DEFAULT, 0)
        repository.reviveNote(note,newStaleDate){
            note.staleDate = newStaleDate
            note.computeStaleProgress()
            val notes = orderNotes(notes.value?.toMutableList() ?: ArrayList()).toMutableList()
            Handler(Looper.getMainLooper()).run {
                _notes.value = notes
            }
        }
    }

    companion object {
        private const val TAG = "NotesViewModel"
        private const val STALE_SEC_DEFAULT = 6 * 24 * 60 * 60//6 days
    }
}