package com.aarav.geowav.core.managers

import android.os.StrictMode

object StrictModeHelper {

    fun enable() {
        enableThreadPolicy()
        enableVmPolicy()
    }

    private fun enableThreadPolicy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()        // detect everything
                .penaltyLog()      // log violations
                .build()
        )
    }

    private fun enableVmPolicy() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}