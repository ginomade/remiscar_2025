package com.nomade.movilremiscar.remiscarmovil.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.nomade.movilremiscar.remiscarmovil.R


class UserDialogFragment : DialogFragment() {
    var key = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.mensaje_disclosure))
                .setPositiveButton("Aceptar") { dialog, id ->
                    if (key.isNotEmpty()) {
                        SharedPrefsUtil.set(key, true)
                    }
                    dismiss()
                }
                .setNegativeButton("Denegar") { dialog, id ->
                    dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setKeyType(setkey: String) {
        key = setkey
    }

}