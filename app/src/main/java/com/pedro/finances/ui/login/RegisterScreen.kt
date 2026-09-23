package com.pedro.finances.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper

@Composable
fun RegisterScreen(
    dbHelper: DatabaseHelper,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var cpfState by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Back arrow
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color(0xFFFFD700)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CADASTRO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Celular ou e-mail") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = cpfState,
                onValueChange = { cpfState = handleCpfVisualTransformation(it) },
                label = { Text("CPF") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha / Chave de Acesso") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (isSuccess) Color(0xFF4CAF50) else Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val currentCpf = cpfState.text
                    if (name.isNotBlank() && currentCpf.isNotBlank() && password.isNotBlank()) {
                        val success = dbHelper.addUser(currentCpf, password, name, emailOrPhone)
                        if (success) {
                            isSuccess = true
                            message = "Cadastro realizado com sucesso!"
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onRegisterSuccess()
                            }, 1000)
                        } else {
                            isSuccess = false
                            message = "Erro ao cadastrar (CPF já cadastrado?)"
                        }
                    } else {
                        isSuccess = false
                        message = "Preencha todos os campos obrigatórios."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "CADASTRAR",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
