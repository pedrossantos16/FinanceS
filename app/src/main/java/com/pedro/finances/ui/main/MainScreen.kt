package com.pedro.finances.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pedro.finances.data.DatabaseHelper
import com.pedro.finances.ui.screens.AtualizacoesScreen
import com.pedro.finances.ui.screens.InicioScreen
import com.pedro.finances.ui.screens.LancamentosScreen
import com.pedro.finances.ui.screens.MetasScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "Início", Icons.Default.Home)
    object Metas : Screen("metas", "Metas", Icons.Default.Star)
    object Lancamentos : Screen("lancamentos", "Lançar", Icons.Default.Add)
    object Atualizacoes : Screen("atualizacoes", "Atualizações", Icons.Default.Refresh)
}

@Composable
fun MainScreen(dbHelper: DatabaseHelper) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Inicio) }
    val screens = listOf(Screen.Inicio, Screen.Metas, Screen.Lancamentos, Screen.Atualizacoes)

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            // Floating curved bottom navigation bar with selected icon background pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(32.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        screens.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val backgroundColor = if (isSelected) Color(0xFF2C2C2C) else Color.Transparent
                            val tintColor = if (isSelected) Color(0xFFFFD700) else Color.Gray

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(backgroundColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { currentScreen = screen }) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = tintColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Inicio -> InicioScreen(dbHelper)
                is Screen.Metas -> MetasScreen(dbHelper)
                is Screen.Lancamentos -> LancamentosScreen(dbHelper)
                is Screen.Atualizacoes -> AtualizacoesScreen()
            }
        }
    }
}
