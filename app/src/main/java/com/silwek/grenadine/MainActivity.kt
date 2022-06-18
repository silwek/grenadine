package com.silwek.grenadine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.silwek.grenadine.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInit = false
    private val adapter: NotesAdapter by lazy { NotesAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.listNotes.layoutManager = LinearLayoutManager(this)
        binding.listNotes.adapter = adapter

        adapter.onWantDelete = this::deleteNote
        binding.addNote.setOnClickListener { addNote() }
    }

    override fun onStart() {
        super.onStart()
        if (!isInit) {
            getNotesViewModel().notes.observe(this, this::onNotes)
            isInit = true
        }
        getNotesViewModel().loadNotes()
    }

    private fun onNotes(notes: List<Note>) {
        adapter.items = notes
    }

    private fun deleteNote(note: Note) {
        val idToDelete = note.id
        if (idToDelete != null)
            getNotesViewModel().removeNote(idToDelete)
    }

    private fun addNote() {
        getNotesViewModel().addNote("New note ${getRandomString(6)}")
    }

    private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    private fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }

}