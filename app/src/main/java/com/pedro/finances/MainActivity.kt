package com.pedro.finances

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.pedro.finances.data.DatabaseHelper
import com.pedro.finances.ui.login.LoginScreen
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
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(
                        dbHelper = dbHelper,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                } else {
                    MainScreen(dbHelper = dbHelper)
                }
            }
        }
    }
}
