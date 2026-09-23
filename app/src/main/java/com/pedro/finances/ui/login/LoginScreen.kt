package com.pedro.finances.ui.login

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper

fun handleCpfVisualTransformation(input: TextFieldValue): TextFieldValue {
    val digits = input.text.filter { it.isDigit() }.take(11)
    val sb = StringBuilder()
    
    val originalSelection = input.selection.start
    val digitsBeforeCursor = input.text.take(originalSelection).count { it.isDigit() }

    var newCursorIndex = 0
    var digitsProcessed = 0

    for (i in digits.indices) {
        if (digitsProcessed == 3 || digitsProcessed == 6) {
            sb.append('.')
            if (digitsProcessed <= digitsBeforeCursor) newCursorIndex++
        } else if (digitsProcessed == 9) {
            sb.append('-')
            if (digitsProcessed <= digitsBeforeCursor) newCursorIndex++
        }
        sb.append(digits[i])
        if (digitsProcessed < digitsBeforeCursor) {
            newCursorIndex++
        }
        digitsProcessed++
    }

    val formatted = sb.toString()
    newCursorIndex = newCursorIndex.coerceIn(0, formatted.length)
    return TextFieldValue(text = formatted, selection = TextRange(newCursorIndex))
}

@Composable
fun LoginScreen(
    dbHelper: DatabaseHelper,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE) }

    val rememberedCpf = remember { sharedPreferences.getString("saved_cpf", "") ?: "" }
    var cpfState by remember { mutableStateOf(TextFieldValue(rememberedCpf)) }
    var accessKey by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(rememberedCpf.isNotEmpty()) }
    var errorMessage by remember { mutableStateOf("") }

    // Forgot password states
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableStateOf(1) } // 1: Enter CPF, 2: Enter code & new password
    var forgotCpfState by remember { mutableStateOf(TextFieldValue("")) }
    var generatedCode by remember { mutableStateOf("") }
    var enteredCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var forgotMessage by remember { mutableStateOf("") }

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
                value = cpfState,
                onValueChange = { cpfState = handleCpfVisualTransformation(it) },
                label = { Text("CPF", color = Color.DarkGray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFC7A800),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFC7A800),
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accessKey,
                onValueChange = { accessKey = it },
                label = { Text("CHAVE DE ACESSO", color = Color.DarkGray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFC7A800),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFC7A800),
                    unfocusedLabelColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember me checkbox & Forgot password opposite side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD700))
                    )
                    Text(text = "Lembre-se de mim", color = Color.DarkGray, fontSize = 12.sp)
                }

                TextButton(onClick = {
                    forgotStep = 1
                    forgotCpfState = TextFieldValue("")
                    enteredCode = ""
                    newPassword = ""
                    forgotMessage = ""
                    showForgotPasswordDialog = true
                }) {
                    Text(text = "Esqueci minha senha?", color = Color(0xFF1976D2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val currentCpf = cpfState.text
                    if ((currentCpf == "12345678900" && accessKey == "1234") || dbHelper.validateUser(currentCpf, accessKey)) {
                        if (rememberMe) {
                            sharedPreferences.edit().putString("saved_cpf", currentCpf).apply()
                        } else {
                            sharedPreferences.edit().remove("saved_cpf").apply()
                        }
                        onLoginSuccess(currentCpf)
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

            TextButton(onClick = onNavigateToRegister) {
                Text(text = "Criar nova conta / Cadastro", color = Color.DarkGray, fontSize = 13.sp)
            }
        }

        Text(
            text = com.pedro.finances.BuildConfig.VERSION_NAME,
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Recuperação de Conta", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (forgotStep == 1) {
                        Text("Digite o CPF cadastrado para receber o código de verificação por e-mail:", color = Color.White, fontSize = 13.sp)
                        OutlinedTextField(
                            value = forgotCpfState,
                            onValueChange = { forgotCpfState = handleCpfVisualTransformation(it) },
                            label = { Text("CPF") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        if (forgotMessage.isNotEmpty()) {
                            Text(text = forgotMessage, color = Color.Red, fontSize = 12.sp)
                        }
                    } else {
                        Text("Digite o código de 4 dígitos enviado ao seu e-mail e sua nova Chave de Acesso:", color = Color.White, fontSize = 13.sp)
                        if (forgotMessage.isNotEmpty()) {
                            Text(text = forgotMessage, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = enteredCode,
                            onValueChange = { enteredCode = it },
                            label = { Text("Código de Verificação") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nova Chave de Acesso") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (forgotStep == 1) {
                    Button(
                        onClick = {
                            val fCpf = forgotCpfState.text
                            if (fCpf == "12345678900" || dbHelper.userExists(fCpf)) {
                                generatedCode = (1000..9999).random().toString()
                                forgotMessage = "Código enviado para o e-mail cadastrado! (Teste: $generatedCode)"
                                forgotStep = 2
                            } else {
                                forgotMessage = "CPF não encontrado no sistema."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("Enviar Código", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            val fCpf = forgotCpfState.text
                            if (enteredCode == generatedCode && newPassword.isNotBlank()) {
                                dbHelper.updateAccessKey(fCpf, newPassword)
                                forgotMessage = "Senha redefinida com sucesso!"
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    showForgotPasswordDialog = false
                                }, 1200)
                            } else {
                                forgotMessage = "Código incorreto ou nova senha inválida."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("Redefinir Senha", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}
