package com.silwek.grenadine.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.silwek.grenadine.models.Note
import com.silwek.grenadine.R
import com.silwek.grenadine.databinding.ViewFormNoteBinding
import com.silwek.grenadine.viewmodels.getNotesViewModel

class NewNoteDialogFragment : DialogFragment() {

    private var note: Note? = null
    private lateinit var binding: ViewFormNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewFormNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btSave.setOnClickListener {
            save()
        }
        binding.root.setOnClickListener { dismissAllowingStateLoss() }
        binding.btClose.setOnClickListener { dismissAllowingStateLoss() }
        binding.label.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onStart() {
        super.onStart()
        note = getNotesViewModel()?.editNote
        if (note == null) {
            binding.btSave.text = "Ajouter"
        } else {
            binding.btSave.text = "Modifier"
            binding.label.setText(note?.label)
        }
        binding.label.requestFocus()
        Handler(Looper.getMainLooper()).post {
            showKeyboard(binding.label)
        }
    }

    private fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun save(): Boolean {
        if (!isValid()) return false
        val n = note
        if (n == null)
            getNotesViewModel()?.addNote(binding.label.text.toString())
        else
            getNotesViewModel()?.updateNote(binding.label.text.toString(), n)
        dismissAllowingStateLoss()
        return true
    }

    private fun isValid(): Boolean {
        return binding.label.text.toString().isNotEmpty()
    }

    companion object {
        fun showDialog(activity: AppCompatActivity) {
            showDialog(
                activity.supportFragmentManager,
                activity.resources.getBoolean(R.bool.large_layout)
            )
        }

        private fun showDialog(fragmentManager: FragmentManager, isLargeLayout: Boolean) {
            val newFragment = NewNoteDialogFragment()
            if (isLargeLayout) {
                // The device is using a large layout, so show the fragment as a dialog
                newFragment.show(fragmentManager, "dialog")
            } else {
                // The device is smaller, so show the fragment fullscreen
                val transaction = fragmentManager.beginTransaction()
                // For a little polish, specify a transition animation
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                // To make it fullscreen, use the 'content' root view as the container
                // for the fragment, which is always the root view for the activity
                transaction
                    .add(android.R.id.content, newFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}