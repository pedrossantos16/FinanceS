package com.pedro.finances.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ViewStream
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
    var selectedYear by remember { mutableStateOf(2026) }
    var expanded by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf(dbHelper.getTransactions(selectedMonth)) }
    var isCompactView by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, selectedYear) {
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
            text = "INÍCIO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Month & Year Selector Dropdown
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { expanded = true },
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$selectedMonth / $selectedYear",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Selecionar Mês/Ano",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                // Year selector inside dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedYear -= 1 }) {
                        Text("<", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(text = "Ano: $selectedYear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = { selectedYear += 1 }) {
                        Text(">", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                HorizontalDivider(color = Color.DarkGray)

                val monthsList = listOf(
                    "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
                    "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
                )
                monthsList.forEach { m ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                m,
                                color = if (selectedMonth == m) Color(0xFFFFD700) else Color.White,
                                fontWeight = if (selectedMonth == m) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedMonth = m
                            expanded = false
                        }
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

        // Header with View Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lançamentos de $selectedMonth",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isCompactView = false },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewStream,
                        contentDescription = "Detalhes",
                        tint = if (!isCompactView) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { isCompactView = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Compacto",
                        tint = if (isCompactView) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(transactions) { tx ->
                val cardColor = if (isCompactView) {
                    if (tx.type == "INCOME") Color(0xFF1B3B22) else Color(0xFF3B1B1B)
                } else {
                    Color(0xFF1E1E1E)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCompactView) {
                        // Compact List View: just name, card colored by type (green for income, red for expense)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = tx.category,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Detailed Expanded View: category, date, and amount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tx.category,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val displayDate = if (selectedMonth == "AGOSTO") "15/08/$selectedYear" else "15/09/$selectedYear"
                                Text(
                                    text = "Data: $displayDate",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
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
