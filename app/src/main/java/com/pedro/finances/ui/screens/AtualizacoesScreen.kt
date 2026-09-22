package com.pedro.finances.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AtualizacoesScreen() {
    val context = LocalContext.current
    val currentVersion = "v01.00"
    var updateStatus by remember { mutableStateOf("Clique em verificar para checar atualizações no GitHub") }
    var latestVersion by remember { mutableStateOf("") }
    var releaseUrl by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ATUALIZAÇÕES DO APLICATIVO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Versão Atual Instalada", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = currentVersion, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.DarkGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = updateStatus,
                    color = if (latestVersion.isNotEmpty() && latestVersion != currentVersion) Color(0xFFFFD700) else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isChecking = true
                updateStatus = "Verificando no GitHub..."
                coroutineScope.launch {
                    // Simulating or querying GitHub releases API (e.g. your repository)
                    // You can replace with your GitHub API endpoint: https://api.github.com/repos/SEU_USER/FinanceS/releases/latest
                    val result = withContext(Dispatchers.IO) {
                        try {
                            // For demo demonstration: we simulate finding v01.01 or up to date
                            // In production, parse JSON from GitHub API or check version tag.
                            Thread.sleep(1200)
                            // Simulated check result: let's pretend v01.01 is available or up to date
                            Triple(true, "v01.01", "https://github.com/")
                        } catch (e: Exception) {
                            Triple(false, currentVersion, "")
                        }
                    }

                    isChecking = false
                    if (result.first && result.second != currentVersion) {
                        latestVersion = result.second
                        releaseUrl = result.third
                        updateStatus = "Atualização $latestVersion disponível!"
                    } else {
                        updateStatus = "Sem atualizações disponíveis"
                        latestVersion = ""
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChecking
        ) {
            if (isChecking) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
            } else {
                Text(text = "VERIFICAR ATUALIZAÇÕES", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (latestVersion.isNotEmpty() && latestVersion != currentVersion) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "BAIXAR ATUALIZAÇÃO ($latestVersion)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
