package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.LoginState
import com.work.campvoiceus.viewmodels.LoginViewModel
import com.work.campvoiceus.viewmodels.LoginViewModelFactory

@Composable
fun LoginScreen(
    tokenManager: TokenManager,
    onLoginSuccess: (String) -> Unit, // Callback for successful login
    onNavigateToRegister: () -> Unit // Callback for navigating to the registration page
) {
    val factory = LoginViewModelFactory(tokenManager)
    val viewModel: LoginViewModel = viewModel(factory = factory)

    // Observe login state from the ViewModel
    val loginState by viewModel.loginState.collectAsState()

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "campvoiceus",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(CenterHorizontally)
        )

        Spacer(Modifier.height(30.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Link to navigate to registration
        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Text("Don't have an account? Register here")
        }
    }
}
