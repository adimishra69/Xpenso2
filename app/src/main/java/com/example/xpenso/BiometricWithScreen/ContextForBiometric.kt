package com.example.xpenso.BiometricWithScreen

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/* ---------------------------------------------------------
   Helper function to find the FragmentActivity from context.
   Used by the Biometric logic to find a valid host for the prompt.
---------------------------------------------------------- */
tailrec fun Context.findActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
