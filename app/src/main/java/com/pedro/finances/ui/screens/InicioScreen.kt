package com.pedro.finances.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.pedro.finances.data.TransactionModel

@Composable
fun InicioScreen(dbHelper: DatabaseHelper) {
    var selectedMonth by remember { mutableStateOf("AGOSTO") }
    var transactions by remember { mutableStateOf(dbHelper.getTransactions(selectedMonth)) }

    LaunchedEffect(selectedMonth) {
        transactions = dbHelper.getTransactions(selectedMonth)
    }

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val sobra = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "INÍCIO - FINANÇAS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Month Selector
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

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("Receitas", "R$ %.2f".format(totalIncome), Color(0xFF4CAF50), Modifier.weight(1f))
            SummaryCard("Despesas", "R$ %.2f".format(totalExpense), Color(0xFFE53935), Modifier.weight(1f))
            SummaryCard("Sobrou", "R$ %.2f".format(sobra), if (sobra >= 0) Color(0xFFFFD700) else Color(0xFFE53935), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Lançamentos de $selectedMonth",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = tx.category, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = tx.description, color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            text = "${if (tx.type == "INCOME") "+" else "-"} R$ %.2f".format(tx.amount),
                            color = if (tx.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFE53935),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
