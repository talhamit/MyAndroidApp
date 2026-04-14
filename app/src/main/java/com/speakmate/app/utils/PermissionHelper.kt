package com.speakmate.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val RECORD_AUDIO_REQUEST_CODE = 101

    fun hasAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    fun requestAudioPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    fun isPermissionGranted(requestCode: Int, grantResults: IntArray): Boolean {
        return requestCode == RECORD_AUDIO_REQUEST_CODE &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}
