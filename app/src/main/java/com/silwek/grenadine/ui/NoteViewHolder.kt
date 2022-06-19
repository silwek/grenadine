package com.silwek.grenadine.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.silwek.grenadine.models.Note
import com.silwek.grenadine.databinding.ItemNoteBinding

class NoteViewHolder(
    private val binding: ItemNoteBinding
) : RecyclerView.ViewHolder(binding.root) {

    var staleProgress: Float = 0f

    fun bind(
        note: Note,
        expanded: Boolean,
        onExpandChange: (Note, Boolean) -> Unit,
        onWantDelete: (Note) -> Unit,
        onWantEdit: (Note) -> Unit,
        onWantRevive: (Note) -> Unit
    ) {
        binding.label.text = note.label
        staleProgress = note.staleProgress
        binding.root.setOnClickListener {
            switchExpand()
            onExpandChange(note, isExpand())
        }
        binding.btDelete.setOnClickListener { onWantDelete(note) }
        binding.btReviveDefault.visibility =
            if (note.staleProgress >= 0.5f) View.VISIBLE else View.GONE
        binding.btReviveDefault.setOnClickListener { onWantRevive(note) }
        binding.btEdit.setOnClickListener { onWantEdit(note) }
        binding.root.alpha = if (expanded) 1f else getStaleAlpha()
        binding.expandedContent.visibility = if (expanded) View.VISIBLE else View.GONE
    }

    private fun getStaleAlpha(): Float {
        return 0.3f + ((1f - staleProgress) * 0.7f)
    }

    private fun switchExpand() {
        if (isExpand()) {
            binding.expandedContent.visibility = View.GONE
            binding.root.alpha = getStaleAlpha()
        } else {
            binding.expandedContent.visibility = View.VISIBLE
            binding.root.alpha = 1f
        }
    }

    private fun isExpand(): Boolean {
        return binding.expandedContent.visibility == View.VISIBLE
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): NoteViewHolder {
            return NoteViewHolder(ItemNoteBinding.inflate(inflater, parent, false))
        }
    }
}