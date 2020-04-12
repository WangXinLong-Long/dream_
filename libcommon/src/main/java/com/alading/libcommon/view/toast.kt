package com.alading.libcommon.view

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alading.libcommon.global.AppGlobals

fun Activity.toast(message: CharSequence) =
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()


fun Fragment.toast(message: CharSequence) =
        Toast.makeText(AppGlobals.getApplication().applicationContext, message, Toast.LENGTH_SHORT).show()