package com.silwek.grenadine

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.silwek.grenadine.databinding.ItemNoteBinding

class NotesAdapter(context: Context) : RecyclerView.Adapter<NoteViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private var _items: MutableList<Note> = ArrayList()
    var items: List<Note>
        get() = _items
        set(value) {
            _items.clear()
            _items.addAll(value)
            notifyDataSetChanged()
        }
    var onWantDelete: ((Note) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(_items[position]) {
            onWantDelete?.invoke(it)
        }
    }

    override fun getItemCount(): Int {
        return _items.size
    }
}

class NoteViewHolder(
    private val binding: ItemNoteBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(note: Note, onWantDelete: (Note) -> Unit) {
        binding.label.text = note.label
        //binding.staleBg.alpha = note.staleProgress
        binding.btDelete.setOnClickListener { onWantDelete(note) }
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): NoteViewHolder {
            return NoteViewHolder(ItemNoteBinding.inflate(inflater, parent, false))
        }
    }
}