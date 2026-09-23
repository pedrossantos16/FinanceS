package com.pedro.finances.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
fun AtualizacoesScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE) }

    var repoOwner by remember { mutableStateOf(sharedPreferences.getString("github_owner", "seu-usuario") ?: "seu-usuario") }
    var repoName by remember { mutableStateOf(sharedPreferences.getString("github_repo", "FinanceS") ?: "FinanceS") }
    var showConfigDialog by remember { mutableStateOf(false) }

    val currentVersion = com.pedro.finances.BuildConfig.VERSION_NAME
    var updateStatus by remember { mutableStateOf("Clique em verificar para checar atualizações") }
    var latestVersion by remember { mutableStateOf("") }
    var releaseUrl by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    var showAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CONFIGURAÇÕES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Update Mechanism Card (Top)
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

                HorizontalDivider(color = Color.DarkGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = updateStatus,
                    color = if (latestVersion.isNotEmpty() && latestVersion != currentVersion) Color(0xFFFFD700) else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showConfigDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
        ) {
            Text("Configurar Repositório GitHub ($repoOwner/$repoName)", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (repoOwner == "seu-usuario") {
                    updateStatus = "Configure seu usuário do GitHub acima!"
                    return@Button
                }

                isChecking = true
                updateStatus = "Verificando no GitHub..."
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        try {
                            val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                            
                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "GET"
                                setRequestProperty("Accept", "application/vnd.github.v3+json")
                                connectTimeout = 4000
                                readTimeout = 4000
                            }

                            if (connection.responseCode == 200) {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                val tagRegex = "\"tag_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val match = tagRegex.find(response)
                                val tagName = match?.groups?.get(1)?.value ?: currentVersion

                                val htmlUrlRegex = "\"html_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val htmlMatch = htmlUrlRegex.find(response)
                                val htmlUrl = htmlMatch?.groups?.get(1)?.value ?: "https://github.com/$repoOwner/$repoName/releases"

                                if (tagName != currentVersion) {
                                    Triple(true, tagName, htmlUrl)
                                } else {
                                    Triple(false, currentVersion, "")
                                }
                            } else {
                                Triple(false, currentVersion, "")
                            }
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

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Clickable Card: Conta (Above Sobre o FinanceS)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAccountDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Conta", tint = Color(0xFFFFD700))
                    Text(
                        text = "Gerenciamento de Conta",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = ">", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Clickable Card: Sobre o FinanceS (Last)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAboutDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Sobre", tint = Color(0xFFFFD700))
                    Text(
                        text = "Sobre o FinanceS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = ">", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // GitHub Config Dialog
    if (showConfigDialog) {
        var tempOwner by remember { mutableStateOf(repoOwner) }
        var tempRepo by remember { mutableStateOf(repoName) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Configurar Repositório GitHub", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Insira seu nome de usuário do GitHub e o nome do repositório onde criou o Release:", color = Color.White, fontSize = 13.sp)
                    OutlinedTextField(
                        value = tempOwner,
                        onValueChange = { tempOwner = it },
                        label = { Text("Usuário do GitHub") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = tempRepo,
                        onValueChange = { tempRepo = it },
                        label = { Text("Nome do Repositório") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repoOwner = tempOwner
                        repoName = tempRepo
                        sharedPreferences.edit().putString("github_owner", repoOwner).putString("github_repo", repoName).apply()
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }

    // Account Management Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Gerenciamento de Conta", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                    ) {
                        Text("Adicionar / Trocar Conta")
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                    ) {
                        Text("Editar Login / Chave de Acesso")
                    }
                    Button(
                        onClick = {
                            showAccountDialog = false
                            showLogoutDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sair da Conta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Fechar", color = Color(0xFFFFD700))
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Sobre o FinanceS", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Objetivo do Aplicativo:\nO FinanceS foi desenvolvido para ajudar na organização e no planejamento financeiro pessoal, permitindo o controle rigoroso de receitas, despesas e metas com base em modelos de planilhas avançados.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    HorizontalDivider(color = Color.DarkGray)
                    Text(
                        text = "Descrição das Abas de Navegação:",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TabDescriptionItem("• Início:", "Visão geral do mês/ano selecionado com total de receitas, despesas, saldo livre ('Sobrou') e listagem de lançamentos com opção de visualização detalhada ou compacta colorida por tipo.")
                    TabDescriptionItem("• Metas:", "Gerenciamento de metas financeiras com tabela editável de categorias, valores de meta e tolerância. Suporte a adição, duplicação e exclusão de itens.")
                    TabDescriptionItem("• Análise:", "Área dedicada a futuros gráficos e cruzamentos estatísticos baseados nos dados financeiros e nas metas cadastradas.")
                    TabDescriptionItem("• Lançar:", "Cadastro rápido de receitas e despesas com histórico recente rolável.")
                    TabDescriptionItem("• Configurações:", "Gerenciamento de atualizações via GitHub, gerenciamento de conta e informações gerais sobre o aplicativo.")
                    HorizontalDivider(color = Color.DarkGray)
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Criador: Pedro",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Data de criação: Setembro de 2026",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Fechar", color = Color(0xFFFFD700))
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Confirmar Saída da Conta", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente sair da conta e retornar à tela de login?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Sair", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
fun TabDescriptionItem(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = description, color = Color(0xFFB0B0B0), fontSize = 12.sp)
    }
}
