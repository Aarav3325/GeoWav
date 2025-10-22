package com.aarav.geowav.presentation.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Test{
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date())
    }
}
