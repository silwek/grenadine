package com.silwek.grenadine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.silwek.grenadine.databinding.ActivityMainBinding
import com.silwek.grenadine.models.Note
import com.silwek.grenadine.viewmodels.NotesViewModel
import com.silwek.grenadine.viewmodels.getNotesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInit = false
    private val adapter: NotesAdapter by lazy { NotesAdapter(this) }
    private val notesViewModel: NotesViewModel by lazy { getNotesViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.listNotes.layoutManager = LinearLayoutManager(this)
        binding.listNotes.adapter = adapter

        adapter.onWantDelete = this::deleteNote
        adapter.onWantEdit = this::editNote
        adapter.onWantRevive = this::reviveNote
        binding.addNote.setOnClickListener { addNote() }
        binding.refreshListNotes.isRefreshing = false
        binding.refreshListNotes.setOnRefreshListener {
            notesViewModel.loadNotes()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isInit) {
            binding.refreshListNotes.isRefreshing = true
            notesViewModel.notes.observe(this, this::onNotes)
            isInit = true
        }
        notesViewModel.loadNotes()
    }

    private fun onNotes(notes: List<Note>) {
        adapter.updateItems(notes) {
            binding.refreshListNotes.isRefreshing = false
        }
    }

    private fun deleteNote(note: Note) {
        notesViewModel.removeNote(note)
    }

    private fun addNote() {
        getNotesViewModel().editNote = null
        NewNoteDialogFragment.showDialog(this)
    }

    private fun editNote(note: Note) {
        getNotesViewModel().editNote = note
        NewNoteDialogFragment.showDialog(this)
    }

    private fun reviveNote(note: Note) {
        getNotesViewModel().reviveNote(note)
    }

}