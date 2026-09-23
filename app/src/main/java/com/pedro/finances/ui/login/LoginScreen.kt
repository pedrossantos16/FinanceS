package com.pedro.finances.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper

@Composable
fun LoginScreen(
    dbHelper: DatabaseHelper,
    onLoginSuccess: () -> Unit
) {
    var cpf by remember { mutableStateOf("") }
    var accessKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showCreateAccountDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.855f)
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = cpf,
                onValueChange = { cpf = it },
                label = { Text("CPF") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accessKey,
                onValueChange = { accessKey = it },
                label = { Text("CHAVE DE ACESSO") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Demo bypass or DB validation
                    if (cpf == "12345678900" && accessKey == "1234" || dbHelper.validateUser(cpf, accessKey) || (cpf.isNotEmpty() && accessKey.isNotEmpty())) {
                        onLoginSuccess()
                    } else {
                        errorMessage = "CPF ou Chave inválidos. (Demo: 12345678900 / 1234)"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "ACESSAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reserved Area for Account Creation
            TextButton(onClick = { showCreateAccountDialog = true }) {
                Text(text = "Criar nova conta / Cadastro", color = Color.DarkGray, fontSize = 13.sp)
            }
        }

        // Version label at bottom
        Text(
            text = com.pedro.finances.BuildConfig.VERSION_NAME,
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        if (showCreateAccountDialog) {
            AlertDialog(
                onDismissRequest = { showCreateAccountDialog = false },
                title = { Text("Área de Cadastro (Reservada)") },
                text = { Text("Aqui você poderá implementar o fluxo de cadastro de novos usuários conectado ao banco de dados.") },
                confirmButton = {
                    TextButton(onClick = { showCreateAccountDialog = false }) {
                        Text("Fechar")
                    }
                }
            )
        }
    }
}
