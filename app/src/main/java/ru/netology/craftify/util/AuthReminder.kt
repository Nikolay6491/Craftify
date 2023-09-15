package ru.netology.craftify.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.craftify.R

object AuthReminder {
    fun remind(view: View, message: String, fragment: Fragment){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.sign_in_error) { findNavController(fragment).navigate(R.id.action_feedFragment_to_signInFragment) }
            .show()
    }
}