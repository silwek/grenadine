package com.silwek.grenadine.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silwek.grenadine.datasource.RoomRepository
import com.silwek.grenadine.models.Note
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NotesViewModel(context: Context) : ViewModel() {
    private val repository = RoomRepository(context)
    private val _notes: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val notes: LiveData<MutableList<Note>> = _notes
    var editNote: Note? = null

    fun loadNotes() {
        viewModelScope.launch {
            repository.loadNotes { notes ->
                computeAllStaleProgress(notes)
                val finalList = removeStaledNotes(notes).toMutableList()
                _notes.value = finalList
            }
        }
    }

    fun addNote(label: String) {
        viewModelScope.launch {
            val note = Note(
                label = label,
                creationDate = LocalDateTime.now(),
                staleDate = LocalDateTime.now().plusDays(STALE_DAY_DEFAULT)
            )
            repository.addNote(note) { id ->
                val notes = notes.value?.toMutableList() ?: ArrayList(1)
                note.id = id
                note.computeStaleProgress()
                notes.add(0, note)
                _notes.value = notes
            }
        }

    }

    fun removeNote(note: Note) {
        viewModelScope.launch {
            repository.removeNote(note) {
                _notes.value = _notes.value?.apply { removeIf { it.id == note.id } }
            }
        }
    }

    private fun removeNotes(notes: List<Note>) {
        viewModelScope.launch {
            repository.removeNotes(notes) {
                Log.d(TAG, "Staled notes successfully deleted!")
            }
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
        viewModelScope.launch {
            repository.updateNote(newLabel, note) { newNote ->
                replaceNote(newNote, false)
            }
        }
    }

    fun reviveNote(note: Note) {
        viewModelScope.launch {
            val newStaleDate = LocalDateTime.from(note.staleDate).plusDays(STALE_DAY_DEFAULT)
            repository.reviveNote(note, newStaleDate) { newNote ->
                replaceNote(newNote, true)
            }
        }
    }

    private fun replaceNote(note: Note, sort: Boolean) {
        val notes = notes.value?.toMutableList() ?: ArrayList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            notes[index] = note
        }
        _notes.value = if (sort) orderNotes(notes).toMutableList() else notes
    }

    companion object {
        private const val TAG = "NotesViewModel"
        private const val STALE_DAY_DEFAULT = 7L
    }
}