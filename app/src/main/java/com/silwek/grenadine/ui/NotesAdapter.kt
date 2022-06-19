package com.silwek.grenadine.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.silwek.grenadine.models.Note

class NotesAdapter(context: Context) : RecyclerView.Adapter<NoteViewHolder>() {

    private val listDiffer: AsyncListDiffer<Note> = AsyncListDiffer(this, createDiffCallback())
    private var listListener: AsyncListDiffer.ListListener<Note>? = null

    private val inflater = LayoutInflater.from(context)
    var expandedItemId: String? = null
    var onWantEdit: ((Note) -> Unit)? = null
    var onWantRevive: ((Note) -> Unit)? = null
    var onWantDelete: ((Note) -> Unit)? = null

    fun updateItems(newList: List<Note>, onRefreshed: (() -> Unit)? = null) {
        if (onRefreshed != null) {
            //Remove old listener
            listListener?.let {
                listDiffer.removeListListener(it)
            }
            //Create new listener
            listListener =
                AsyncListDiffer.ListListener<Note> { _, _ ->
                    onRefreshed.invoke()
                    listListener?.let {
                        listDiffer.removeListListener(it)
                    }
                }
            //Add new listener
            listListener?.let {
                listDiffer.addListListener(it)
            }
        }
        listDiffer.submitList(ArrayList(newList))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val item = getItemAt(position) ?: return
        val isExpanded = expandedItemId == item.id
        holder.bind(item, isExpanded,
            onExpandChange = { note, expanded ->
                if (expanded) {
                    updateItemWithId(expandedItemId)
                    expandedItemId = note.id
                } else if (expandedItemId == note.id) {
                    expandedItemId = null
                }
            },
            onWantDelete = {
                onWantDelete?.invoke(it)
            },
            onWantEdit = {
                onWantEdit?.invoke(it)
            },
            onWantRevive = {
                onWantRevive?.invoke(it)
            })
    }

    private fun updateItemWithId(id: String?) {
        val position = getList().indexOfFirst { it.id == id }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return getList().size
    }

    fun getList(): List<Note> = listDiffer.currentList

    private fun getItemAt(position: Int): Note? {
        if (position in getList().indices) {
            return getList()[position]
        }
        return null
    }

    private fun createDiffCallback(): DiffUtil.ItemCallback<Note> {
        return object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return newItem.id == oldItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return newItem.label == oldItem.label && newItem.staleDate?.equals(oldItem.staleDate) == true
            }
        }
    }
}

