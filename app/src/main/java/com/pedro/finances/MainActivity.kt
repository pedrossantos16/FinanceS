package com.pedro.finances

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.pedro.finances.data.DatabaseHelper
import com.pedro.finances.ui.login.LoginScreen
import com.pedro.finances.ui.login.RegisterScreen
import com.pedro.finances.ui.main.MainScreen
import com.pedro.finances.ui.theme.FinanceSTheme

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        enableEdgeToEdge()

        setContent {
            FinanceSTheme {
                var currentScreen by remember { mutableStateOf("login") } // "login", "register", "main"
                var loggedInCpf by remember { mutableStateOf("") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        dbHelper = dbHelper,
                        onLoginSuccess = { cpf ->
                            loggedInCpf = cpf
                            currentScreen = "main"
                        },
                        onNavigateToRegister = { currentScreen = "register" }
                    )
                    "register" -> RegisterScreen(
                        dbHelper = dbHelper,
                        onRegisterSuccess = { currentScreen = "login" },
                        onBack = { currentScreen = "login" }
                    )
                    "main" -> MainScreen(
                        dbHelper = dbHelper,
                        userCpf = loggedInCpf,
                        onLogout = {
                            loggedInCpf = ""
                            currentScreen = "login"
                        }
                    )
                }
            }
        }
    }
}
