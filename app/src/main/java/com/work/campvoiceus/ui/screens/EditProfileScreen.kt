package com.work.campvoiceus.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.work.campvoiceus.viewmodels.ProfileEditViewModel

@Composable
fun EditProfileScreen(
    viewModel: ProfileEditViewModel,
    onProfileUpdated: () -> Unit
) {
    val profileData by viewModel.profileData.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var avatarUri by remember { mutableStateOf<String?>(null) }

    // File picker launcher (currently not used for updating the profile)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = GetContent()
    ) { uri ->
        avatarUri = uri?.toString()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Avatar Preview
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUri ?: profileData.avatarUrl ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            IconButton(
                onClick = { filePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(imageVector = Icons.Default.Camera, contentDescription = "Edit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name Input
        OutlinedTextField(
            value = profileData.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bio Input
        OutlinedTextField(
            value = profileData.bio,
            onValueChange = { viewModel.updateBio(it) },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.updateProfile(
                    updatedData = profileData
                )
                onProfileUpdated()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}
