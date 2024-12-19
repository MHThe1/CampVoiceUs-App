package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.work.campvoiceus.viewmodels.LoginState
import com.work.campvoiceus.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit // Callback for successful login
) {
    val viewModel: LoginViewModel = viewModel() // ViewModel for handling login logic
    val loginState by viewModel.loginState.collectAsState() // Observe login state

    var identifier by remember { mutableStateOf("") } // User input for email/username
    var password by remember { mutableStateOf("") } // User input for password

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("Email or Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Trigger login attempt in ViewModel
                viewModel.login(identifier, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Handle different states of the login process
        when (loginState) {
            is LoginState.Loading -> CircularProgressIndicator(modifier = Modifier.align(alignment = CenterHorizontally))
            is LoginState.Success -> {
                // Invoke the callback with the token
                onLoginSuccess((loginState as LoginState.Success).token)
            }
            is LoginState.Error -> {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(alignment = CenterHorizontally)
                )
            }
            else -> {
                // Do nothing if state is Idle
            }
        }
    }
}
