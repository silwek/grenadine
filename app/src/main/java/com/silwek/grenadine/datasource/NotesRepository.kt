package com.silwek.grenadine.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.silwek.grenadine.models.Note

class NotesRepository {
    private val userId = "MeKkrfCuTnBWdakuTLzr"
    private val db = Firebase.firestore
    private val notesCollection by lazy {
        db.collection("users").document(userId).collection("notes")
    }

    fun loadNotes(onSuccess: (List<Note>) -> Unit) {
        notesCollection.get()
            .addOnSuccessListener { result ->
                val notes = ArrayList<Note>()
                for (document in result) {
                    val note = documentToNote(document)
                    notes.add(note)
                }
                onSuccess(notes)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting notes.", exception)
            }
    }

    fun addNote(note: Note, onSuccess: (id: String) -> Unit) {

        val noteRequest = hashMapOf(
            "label" to note.label,
            "creationDate" to note.creationDate,
            "staleDate" to note.staleDate,
        )
        notesCollection.add(noteRequest)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Note successfully written!")
                onSuccess(result.id)
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing note", e) }
    }

    fun updateNote(newLabel: String, note: Note, onSuccess: () -> Unit) {
        val id = note.id ?: return
        val noteRequest = hashMapOf(
            "label" to (note.label ?: "")
        ).toMap()
        notesCollection.document(id).update(noteRequest)
            .addOnSuccessListener {
                Log.d(TAG, "Note successfully updated!")
                onSuccess()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating note", e) }
    }

    fun reviveNote(note: Note, newStaleDate: Timestamp, onSuccess: () -> Unit) {
        val id = note.id ?: return
        val noteRequest = hashMapOf(
            "staleDate" to newStaleDate,
        ).toMap()
        notesCollection.document(id).update(noteRequest)
            .addOnSuccessListener {
                Log.d(TAG, "Note successfully updated!")
                onSuccess()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating note", e) }
    }

    fun removeNote(idToDelete: String, onSuccess: () -> Unit) {
        notesCollection.document(idToDelete)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Note with id \"$idToDelete\" successfully deleted!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error deleting note with id \"$idToDelete\"",
                    e
                )
            }
    }

    fun removeNotes(notes: List<Note>, onSuccess: () -> Unit) {
        db.runBatch { batch ->
            notes.forEach { note ->
                note.id?.let { idToDelete ->
                    batch.delete(notesCollection.document(idToDelete))
                }
            }
        }.addOnCompleteListener {
            Log.d(TAG, "Staled notes successfully deleted!")
            onSuccess()
        }
    }

    private fun documentToNote(document: QueryDocumentSnapshot): Note {
        return document.toObject<Note>().apply {
            id = document.id
        }
    }

    companion object {
        private const val TAG = "NotesRepository"
    }
}