package com.aarav.geowav.core.utils

sealed class UploadResult {
    data class Progress(val progress: Float) : UploadResult()
    data class Success(val downloadUrl: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}