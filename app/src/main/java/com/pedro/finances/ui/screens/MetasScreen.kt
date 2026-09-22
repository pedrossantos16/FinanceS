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
import com.pedro.finances.data.GoalModel

@Composable
fun MetasScreen(dbHelper: DatabaseHelper) {
    var selectedMonth by remember { mutableStateOf("AGOSTO") }
    var goals by remember { mutableStateOf(dbHelper.getGoals(selectedMonth)) }

    LaunchedEffect(selectedMonth) {
        goals = dbHelper.getGoals(selectedMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "META FINANCEIRA DO MÊS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C2C2C))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Categoria", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
            Text("Meta", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text("Atingido", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text("Resultado", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(goals) { goal ->
                val resultPercent = if (goal.targetAmount > 0) (goal.achievedAmount / goal.targetAmount) * 100 else 0.0
                val resultColor = if (resultPercent <= 100) Color(0xFF4CAF50) else Color(0xFFE53935)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(goal.category, color = Color.White, modifier = Modifier.weight(1.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("R$ %.2f".format(goal.targetAmount), color = Color.Gray, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text("R$ %.2f".format(goal.achievedAmount), color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(resultColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "%.1f%%".format(resultPercent),
                                color = resultColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
