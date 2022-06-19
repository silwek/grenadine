package com.silwek.grenadine.viewmodels

import android.app.Activity
import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <T : ViewModel?> get(context: Context, modelClass: Class<out T>): T {
            return with(context)[modelClass]
        }

        private fun with(context: Context): ViewModelProvider {
            return ViewModelProvider(AppViewModelStore, ViewModelFactory(context))
        }
    }
}

fun Activity.getNotesViewModel() =
    ViewModelFactory.get(applicationContext, NotesViewModel::class.java)

fun DialogFragment.getNotesViewModel() = getApplicationContext()?.getNotesViewModel()

fun DialogFragment.getApplicationContext(): Activity? {
    val ctx = context
    return if (ctx is Activity) ctx else null
}
