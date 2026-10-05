package com.smooth.translate.data.model

sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progress: Float = 0f) : ModelDownloadState()
    data object Downloaded : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}
