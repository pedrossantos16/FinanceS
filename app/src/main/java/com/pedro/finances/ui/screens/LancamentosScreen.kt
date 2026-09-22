package com.pedro.finances.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper

@Composable
fun LancamentosScreen(dbHelper: DatabaseHelper) {
    var selectedMonth by remember { mutableStateOf("AGOSTO") }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NOVO LANÇAMENTO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("AGOSTO", "SETEMBRO").forEach { month ->
                Button(
                    onClick = { selectedMonth = month },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMonth == month) Color(0xFFFFD700) else Color(0xFF1E1E1E)
                    )
                ) {
                    Text(
                        text = month,
                        color = if (selectedMonth == month) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { type = "EXPENSE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "EXPENSE") Color(0xFFE53935) else Color(0xFF1E1E1E)
                )
            ) {
                Text("Despesa", color = Color.White)
            }
            Button(
                onClick = { type = "INCOME" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "INCOME") Color(0xFF4CAF50) else Color(0xFF1E1E1E)
                )
            ) {
                Text("Receita", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoria (ex: COMBUSTÍVEL, NUBANK)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Valor (R$)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )

        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = successMessage, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (category.isNotEmpty() && parsedAmount != null) {
                    dbHelper.addTransaction(selectedMonth, category.uppercase(), parsedAmount, type, description)
                    successMessage = "Lançamento salvo com sucesso!"
                    category = ""
                    amount = ""
                    description = ""
                } else {
                    successMessage = "Preencha a categoria e um valor válido."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR LANÇAMENTO", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
