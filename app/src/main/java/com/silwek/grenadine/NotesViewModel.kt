package com.silwek.grenadine

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

class NotesViewModel : ViewModel() {
    private val id = "MeKkrfCuTnBWdakuTLzr"
    private val db = Firebase.firestore
    private val notesCollection by lazy { db.collection("users").document(id).collection("notes") }

    private val _notes: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val notes: LiveData<MutableList<Note>> = _notes


    fun loadNotes() {
        notesCollection.get()
            .addOnSuccessListener { result ->
                val notes = ArrayList<Note>()
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    val note = documentToNote(document)
                    notes.add(note)
                }
                computeAllStaleProgress(notes)
                val finalList = removeStaledNotes(notes).toMutableList()
                Handler(Looper.getMainLooper()).run {
                    _notes.value = finalList
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting notes.", exception)
            }
    }

    fun addNote(label: String) {
        val note = Note(
            label = label,
            creationDate = Timestamp(Date()),
            staleDate = Timestamp(Date().toInstant().epochSecond + STALE_SEC_DEFAULT, 0)
        )

        val noteRequest = hashMapOf(
            "label" to note.label,
            "creationDate" to note.creationDate,
            "staleDate" to note.staleDate,
        )
        notesCollection.add(noteRequest)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Note successfully written!")
                val notes = this.notes.value?.toMutableList() ?: ArrayList(1)
                note.id = result.id
                note.computeStaleProgress()
                notes.add(note)
                Handler(Looper.getMainLooper()).run {
                    _notes.value = notes
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing note", e) }
    }

    fun removeNote(idToDelete: String) {
        notesCollection.document(idToDelete)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Note with id \"$idToDelete\" successfully deleted!")
                Handler(Looper.getMainLooper()).run {
                    _notes.value = _notes.value?.apply { removeIf { it.id == idToDelete } }
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting note with id \"$idToDelete\"", e) }
    }

    private fun removeNotes(notes: List<Note>) {
        db.runBatch { batch ->
            notes.forEach { note ->
                note.id?.let { idToDelete ->
                    batch.delete(notesCollection.document(idToDelete))
                }
            }
        }.addOnCompleteListener {
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
        return freshNotes.sortedBy { it.staleProgress }
    }

    private fun documentToNote(document: QueryDocumentSnapshot): Note {
        return document.toObject<Note>().apply {
            id = document.id
        }
    }

    companion object {
        private const val TAG = "NotesViewModel"
        private const val STALE_SEC_DEFAULT = 6 * 24 * 60 * 60//6 days
    }
}