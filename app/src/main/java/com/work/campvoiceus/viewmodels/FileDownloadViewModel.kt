package com.work.campvoiceus.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.os.Environment
import android.widget.Toast

class FileDownloadViewModel : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    fun downloadThreadFile(fileUrl: String, fileName: String, context: Context) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Loading
            try {
                // Create a download manager request
                val request = DownloadManager.Request(Uri.parse(fileUrl))
                    .setTitle(fileName)
                    .setDescription("Downloading $fileName")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                // Get download service and enqueue the request
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)

                _downloadState.value = DownloadState.Success
                Toast.makeText(context, "Download started: $fileName", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.message ?: "Unknown error occurred")
                Toast.makeText(
                    context,
                    "Failed to start download: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}