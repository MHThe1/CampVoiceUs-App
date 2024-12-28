package com.work.campvoiceus.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.work.campvoiceus.viewmodels.CreateThreadViewModel

@Composable
fun CreateThreadScreen(
    viewModel: CreateThreadViewModel = viewModel(),
    onThreadCreated: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    // Get the context
    val context = LocalContext.current

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            val fileType = contentResolver.getType(uri) ?: ""
            val cursor = contentResolver.query(uri, null, null, null, null)
            val fileSize = cursor?.use {
                if (it.moveToFirst()) it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE)) else -1
            } ?: -1

            // Allowed MIME types
            val allowedTypes = listOf("image/", "video/", "application/zip")

            // Check file type and size
            if (allowedTypes.any { fileType.startsWith(it) } && fileSize <= 10 * 1024 * 1024) { // 10 MB
                fileUri = uri
            } else {
                fileUri = null
                Toast.makeText(context, "Invalid file. Only images, videos, or ZIP files under 10 MB are allowed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (isSuccess) {
        onThreadCreated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Create a Thread",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tags Display
        if (tags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags.size) { index ->
                    Chip(
                        text = "#${tags[index]}",
                        onRemove = { tags.removeAt(index) }
                    )
                }
            }
        }

        // Tag Input
        OutlinedTextField(
            value = tagInput,
            onValueChange = { input ->
                tagInput = input
                if (input.endsWith(",")) {
                    val newTag = input.trimEnd(',').trim()
                    if (newTag.isNotBlank() && !tags.contains(newTag)) {
                        tags.add(newTag)
                    }
                    tagInput = ""
                }
            },
            label = { Text("Add Tags (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // File Picker
        Button(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 80.dp, end = 80.dp)
        ) {
            Text(text = "Choose File")
        }

        if (fileUri != null) {
            val fileName = context.contentResolver.query(fileUri!!, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                else "unknown_file"
            } ?: "unknown_file"
            Text(text = "File selected: $fileName")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.createThread(title, content, tags.joinToString(","), fileUri, context) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Submit Thread")
            }
        }
    }
}

@Composable
fun Chip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onRemove() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 4.dp)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove Tag",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}
