package com.pedro.finances.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.finances.data.DatabaseHelper
import com.pedro.finances.data.GoalModel
import com.pedro.finances.data.TransactionModel

@Composable
fun AnaliseScreen(dbHelper: DatabaseHelper, userCpf: String) {
    var selectedYear by remember { mutableStateOf(2026) }
    val transactions = remember { dbHelper.getAllTransactions(userCpf) }
    val goals = remember { dbHelper.getGoals(userCpf) }

    val validGoals = goals.filter { it.category.isNotBlank() }
    val defaultGoalCategory = validGoals.firstOrNull()?.category ?: "GERAL"
    var selectedGoalForLine by remember { mutableStateOf(defaultGoalCategory) }

    // Detail modal state
    var activeDetailChart by remember { mutableStateOf<String?>(null) }
    var showSuggestionsModal by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val shortMonthsList = listOf("JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")
    val fullMonthsList = listOf("JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ANÁLISE E GRÁFICOS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )

            // Year selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedYear -= 1 }) {
                    Text("<", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
                Text(text = "$selectedYear", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = { selectedYear += 1 }) {
                    Text(">", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sugestões Card (Top)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSuggestionsModal = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "💡 Sugestões de Equilíbrio", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Guia para diminuir gastos em X e investir em Y", color = Color.Gray, fontSize = 12.sp)
                }
                Text(text = ">", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isNotEmpty()) {
            // 1. Ganhos vs Gastos por Mês (Column Chart)
            ChartCard(
                title = "1. Total de Ganhos vs Gastos por Mês",
                subtitle = "Toque no gráfico para ver detalhes e tabela",
                onClick = { activeDetailChart = "ganhos_gastos" }
            ) {
                GanhosGastosColumnChart(transactions, fullMonthsList)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Meta vs Atingido no Mês (Line Chart) - Only show if valid goals & transactions exist
        if (validGoals.isNotEmpty() && transactions.isNotEmpty()) {
            ChartCard(
                title = "2. Meta vs Atingido no Mês",
                subtitle = "Categoria: $selectedGoalForLine (Toque para detalhes)",
                onClick = { activeDetailChart = "meta_atingido" }
            ) {
                Column {
                    ScrollableTabRow(
                        selectedTabIndex = validGoals.indexOfFirst { it.category == selectedGoalForLine }.coerceAtLeast(0),
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFD700)
                    ) {
                        validGoals.forEach { g ->
                            Tab(
                                selected = selectedGoalForLine == g.category,
                                onClick = { selectedGoalForLine = g.category },
                                text = { Text(g.category, color = if (selectedGoalForLine == g.category) Color(0xFFFFD700) else Color.Gray) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MetaAtingidoLineChart(transactions, validGoals, selectedGoalForLine, shortMonthsList, fullMonthsList)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (transactions.isNotEmpty()) {
            // 3. Pizza de Gastos sobre a Renda Total (Pie Chart)
            ChartCard(
                title = "3. Percentual de Gastos sobre a Renda (100%)",
                subtitle = "Sobra em destaque verde (Toque para detalhes)",
                onClick = { activeDetailChart = "pizza_gastos" }
            ) {
                GastosPizzaChart(transactions)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Investimentos por Mês (Column Chart)
            ChartCard(
                title = "4. Investimentos por Mês",
                subtitle = "Categoria: INVESTIMENTO (Toque para detalhes)",
                onClick = { activeDetailChart = "investimentos" }
            ) {
                InvestimentosColumnChart(transactions, fullMonthsList)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 5. Tolerância vs Erro por Categoria (Line Chart) - Only show if valid goals & transactions exist
        if (validGoals.isNotEmpty() && transactions.isNotEmpty()) {
            ChartCard(
                title = "5. Tolerância vs Erro (%) em Relação à Meta",
                subtitle = "Comparativo de desvios percentuais (Toque para detalhes)",
                onClick = { activeDetailChart = "tolerancia_erro" }
            ) {
                ToleranciaErroLineChart(transactions, validGoals)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (transactions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nenhum lançamento cadastrado. Adicione transações na aba Lançar para visualizar os gráficos.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Detail Modal / Screen
    if (activeDetailChart != null) {
        ChartDetailModal(
            chartType = activeDetailChart!!,
            transactions = transactions,
            goals = validGoals,
            selectedYear = selectedYear,
            selectedGoalForLine = selectedGoalForLine,
            onDismiss = { activeDetailChart = null }
        )
    }

    // Suggestions Modal
    if (showSuggestionsModal) {
        SugestoesModal(validGoals, transactions, onDismiss = { showSuggestionsModal = false })
    }
}

@Composable
fun SugestoesModal(goals: List<GoalModel>, transactions: List<TransactionModel>, onDismiss: () -> Unit) {
    val suggestions = mutableListOf<String>()
    var totalExcess = 0.0
    var totalDeficit = 0.0

    val effectiveGoals = goals.map { g ->
        if (g.category.isBlank() && goals.isNotEmpty()) goals.first() else g
    }

    effectiveGoals.forEach { goal ->
        val achieved = transactions.filter { it.category.uppercase() == goal.category.uppercase() && it.type == "EXPENSE" }.sumOf { it.amount }
        val diff = achieved - goal.targetAmount
        if (diff > 0 && goal.targetAmount > 0) {
            totalExcess += diff
            suggestions.add("🔴 Reduzir gastos em [${goal.category}]: Você ultrapassou a meta em R$ %.2f (Gastou R$ %.2f de R$ %.2f)".format(diff, achieved, goal.targetAmount))
        } else if (diff < 0 && goal.targetAmount > 0) {
            val deficit = -diff
            totalDeficit += deficit
            suggestions.add("🟢 Oportunidade em [${goal.category}]: Você gastou R$ %.2f a menos que a meta de R$ %.2f. Pode depositar/alocar mais nesse setor.".format(deficit, goal.targetAmount))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Guia de Equilíbrio Financeiro (Sugestões)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Este guia indica onde você precisa diminuir os gastos (X) e onde pode depositar/alocar mais (Y) com base nas suas metas:",
                    color = Color.White,
                    fontSize = 13.sp
                )
                HorizontalDivider(color = Color.DarkGray)

                if (suggestions.isEmpty()) {
                    Text("Nenhuma categoria com meta definida para gerar sugestões. Adicione categorias na aba Metas!", color = Color.Gray)
                } else {
                    suggestions.forEach { s ->
                        Text(text = s, color = Color.White, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color.DarkGray)
                    Text(
                        text = "Resumo do Ajuste:\n• Total a reduzir (X): R$ %.2f\n• Total disponível/poupança (Y): R$ %.2f".format(totalExcess, totalDeficit),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ChartCard(title: String, subtitle: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun GanhosGastosColumnChart(transactions: List<TransactionModel>, fullMonths: List<String>) {
    val incomeData = fullMonths.map { m -> transactions.filter { it.month.uppercase() == m && it.type == "INCOME" }.sumOf { it.amount } }
    val expenseData = fullMonths.map { m -> transactions.filter { it.month.uppercase() == m && it.type == "EXPENSE" }.sumOf { it.amount } }
    val maxVal = (incomeData + expenseData).maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / 36f

        incomeData.forEachIndexed { index, income ->
            val expense = expenseData[index]
            val x = index * (width / 12f) + width / 24f

            val incomeHeight = (income / maxVal * (height - 30f)).toFloat()
            val expenseHeight = (expense / maxVal * (height - 30f)).toFloat()

            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(x - barWidth, height - incomeHeight - 20f),
                size = Size(barWidth, incomeHeight)
            )

            drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(2f, height - expenseHeight - 20f),
                size = Size(barWidth, expenseHeight)
            )
        }
    }
}

@Composable
fun MetaAtingidoLineChart(transactions: List<TransactionModel>, goals: List<GoalModel>, category: String, shortMonths: List<String>, fullMonths: List<String>) {
    val targetVal = goals.find { it.category == category }?.targetAmount ?: 500.0
    val achievedData = fullMonths.map { m ->
        transactions.filter { it.month.uppercase() == m && it.category.uppercase() == category }.sumOf { it.amount }
    }
    val maxVal = (achievedData + targetVal).maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val stepX = width / 12f

        val targetY = height - 20f - (targetVal / maxVal * (height - 40f)).toFloat()

        drawLine(
            color = Color(0xFFFFD700),
            start = Offset(0f, targetY),
            end = Offset(width, targetY),
            strokeWidth = 3f
        )

        val path = Path()
        achievedData.forEachIndexed { index, valAchieved ->
            val x = index * stepX + stepX / 2f
            val y = height - 20f - (valAchieved / maxVal * (height - 40f)).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(Color(0xFF00BCD4), 5f, Offset(x, y))
        }
        drawPath(path, Color(0xFF00BCD4), style = Stroke(width = 4f))
    }
}

@Composable
fun GastosPizzaChart(transactions: List<TransactionModel>) {
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }.coerceAtLeast(1.0)
    val expenses = transactions.filter { it.type == "EXPENSE" }.groupBy { it.category }
    val expenseSums = expenses.mapValues { entry -> entry.value.sumOf { it.amount } }
    val totalExpense = expenseSums.values.sum()
    val surplus = (totalIncome - totalExpense).coerceAtLeast(0.0)

    val items = expenseSums.entries.map { it.key to it.value } + listOf("SOBRA/RESTANTE" to surplus)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2f - 20f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = 0f

        val colors = listOf(Color(0xFFE53935), Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFF4CAF50))

        items.forEachIndexed { index, (_, amount) ->
            val sweepAngle = (amount / totalIncome * 360f).toFloat()
            val color = if (index == items.size - 1) Color(0xFF4CAF50) else colors[index % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun InvestimentosColumnChart(transactions: List<TransactionModel>, fullMonths: List<String>) {
    val invData = fullMonths.map { m ->
        transactions.filter { it.month.uppercase() == m && (it.category.uppercase().contains("INVEST") || it.category.uppercase() == "INVESTIMENTO") }.sumOf { it.amount }
    }
    val maxVal = invData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / 18f

        invData.forEachIndexed { index, inv ->
            val x = index * (width / 12f) + width / 24f
            val barHeight = (inv / maxVal * (height - 30f)).toFloat()

            drawRect(
                color = Color(0xFF00E676),
                topLeft = Offset(x - barWidth / 2f, height - barHeight - 20f),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun ToleranciaErroLineChart(transactions: List<TransactionModel>, goals: List<GoalModel>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val stepX = if (goals.isNotEmpty()) width / goals.size.toFloat() else width

        val pathTol = Path()
        val pathErr = Path()

        goals.forEachIndexed { index, goal ->
            val x = index * stepX + stepX / 2f
            val achieved = transactions.filter { it.category.uppercase() == goal.category.uppercase() && it.type == "EXPENSE" }.sumOf { it.amount }
            val errorPercent = if (goal.targetAmount > 0) ((achieved - goal.targetAmount) / goal.targetAmount) * 100.0 else 0.0
            val tolerancePercent = if (goal.targetAmount > 0) (goal.tolerance / goal.targetAmount) * 100.0 else 0.0

            val centerY = height / 2f
            val scaleY = height / 100f

            val yTol = centerY - (tolerancePercent * scaleY).toFloat()
            val yErr = centerY - (errorPercent * scaleY).toFloat()

            if (index == 0) {
                pathTol.moveTo(x, yTol)
                pathErr.moveTo(x, yErr)
            } else {
                pathTol.lineTo(x, yTol)
                pathErr.lineTo(x, yErr)
            }

            drawCircle(Color(0xFFFFD700), 4f, Offset(x, yTol))
            drawCircle(Color(0xFFFF5722), 4f, Offset(x, yErr))
        }

        drawLine(Color.Gray, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth = 1f)

        drawPath(pathTol, Color(0xFFFFD700), style = Stroke(width = 3f))
        drawPath(pathErr, Color(0xFFFF5722), style = Stroke(width = 3f))
    }
}

@Composable
fun ChartDetailModal(
    chartType: String,
    transactions: List<TransactionModel>,
    goals: List<GoalModel>,
    selectedYear: Int,
    selectedGoalForLine: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "Detalhes do Gráfico: ${chartType.replace("_", " ").uppercase()}",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ano Analisado: $selectedYear",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = Color.DarkGray)

                when (chartType) {
                    "ganhos_gastos" -> {
                        Text("Tabela de Ganhos e Gastos por Mês:", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        val fullMonthsList = listOf("JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO")
                        fullMonthsList.forEach { m ->
                            val inc = transactions.filter { it.month.uppercase() == m && it.type == "INCOME" }.sumOf { it.amount }
                            val exp = transactions.filter { it.month.uppercase() == m && it.type == "EXPENSE" }.sumOf { it.amount }
                            if (inc > 0 || exp > 0) {
                                Text("$m -> Ganhos: R$ %.2f | Gastos: R$ %.2f".format(inc, exp), color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                    "meta_atingido" -> {
                        Text("Meta selecionada: $selectedGoalForLine", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        val goal = goals.find { it.category == selectedGoalForLine }
                        Text("Valor da Meta: R$ %.2f".format(goal?.targetAmount ?: 0.0), color = Color.White)
                        val fullMonthsList = listOf("JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO")
                        fullMonthsList.forEach { m ->
                            val ach = transactions.filter { it.month.uppercase() == m && it.category.uppercase() == selectedGoalForLine }.sumOf { it.amount }
                            if (ach > 0) {
                                Text("$m -> Atingido: R$ %.2f".format(ach), color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                    "pizza_gastos" -> {
                        Text("Distribuição de Gastos sobre Renda Total:", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }.coerceAtLeast(1.0)
                        val expenses = transactions.filter { it.type == "EXPENSE" }.groupBy { it.category }
                        expenses.forEach { (cat, list) ->
                            val sum = list.sumOf { it.amount }
                            val pct = (sum / totalIncome) * 100.0
                            Text("$cat: R$ %.2f (%.1f%% da renda)".format(sum, pct), color = Color.White, fontSize = 13.sp)
                        }
                    }
                    "investimentos" -> {
                        Text("Histórico de Investimentos:", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        val fullMonthsList = listOf("JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO")
                        fullMonthsList.forEach { m ->
                            val inv = transactions.filter { it.month.uppercase() == m && (it.category.uppercase().contains("INVEST") || it.category.uppercase() == "INVESTIMENTO") }.sumOf { it.amount }
                            if (inv > 0) {
                                Text("$m -> Investido: R$ %.2f".format(inv), color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                    "tolerancia_erro" -> {
                        Text("Análise de Tolerância vs Erro por Categoria:", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        goals.forEach { g ->
                            val achieved = transactions.filter { it.category.uppercase() == g.category.uppercase() && it.type == "EXPENSE" }.sumOf { it.amount }
                            val err = if (g.targetAmount > 0) ((achieved - g.targetAmount) / g.targetAmount) * 100.0 else 0.0
                            val tol = if (g.targetAmount > 0) (g.tolerance / g.targetAmount) * 100.0 else 0.0
                            Text("${g.category} -> Meta: R$ %.2f | Erro: %.1f%% | Tolerância: %.1f%%".format(g.targetAmount, err, tol), color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
        }
    )
}
