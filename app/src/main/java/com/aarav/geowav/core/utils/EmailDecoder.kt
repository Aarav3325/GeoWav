package com.aarav.geowav.core.utils

fun encodeEmail(email: String): String =
    email.trim().lowercase().replace(".", ",")

fun decodeEmail(encoded: String): String =
    encoded.replace(",", ".")
