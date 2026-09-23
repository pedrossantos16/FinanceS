package com.pedro.finances.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TransactionModel(
    val id: Long,
    val month: String,
    val category: String,
    val amount: Double,
    val type: String, // INCOME or EXPENSE
    val description: String
)

data class GoalModel(
    val id: Long,
    val category: String,
    val targetAmount: Double,
    val tolerance: Double
)

private data class TxDto(val month: String, val category: String, val amount: Double, val type: String)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "finances.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_USERS = "users"
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_GOALS = "goals"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cpf TEXT UNIQUE,
                access_key TEXT,
                name TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month TEXT,
                category TEXT,
                amount REAL,
                type TEXT,
                description TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_GOALS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT,
                target_amount REAL,
                tolerance REAL
            )
        """)

        // Insert initial test user
        db.execSQL("INSERT INTO $TABLE_USERS (cpf, access_key, name) VALUES ('12345678900', '1234', 'Pedro User')")

        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase) {
        // AGOSTO & SETEMBRO Transactions
        val transactions = listOf(
            TxDto("AGOSTO", "SALÁRIO", 1854.15, "INCOME"),
            TxDto("AGOSTO", "ADIANTAMENTO", 1804.99, "INCOME"),
            TxDto("AGOSTO", "SENAI", 518.43, "EXPENSE"),
            TxDto("AGOSTO", "FATURA INTER", 4.50, "EXPENSE"),
            TxDto("AGOSTO", "COMBUSTÍVEL", 300.00, "EXPENSE"),
            TxDto("AGOSTO", "EMERGÊNCIA", 59.90, "EXPENSE"),
            TxDto("AGOSTO", "LAZER", 297.15, "EXPENSE"),
            TxDto("AGOSTO", "INVESTIMENTO", 600.00, "EXPENSE"),
            TxDto("AGOSTO", "NUBANK", 88.57, "EXPENSE"),
            TxDto("SETEMBRO", "SALÁRIO", 1856.80, "INCOME"),
            TxDto("SETEMBRO", "ADIANTAMENTO", 1153.54, "INCOME"),
            TxDto("SETEMBRO", "TICKET ALIMENTAÇÃO", 510.97, "INCOME"),
            TxDto("SETEMBRO", "GASTO TICKET", 482.96, "EXPENSE"),
            TxDto("SETEMBRO", "SENAI", 518.43, "EXPENSE"),
            TxDto("SETEMBRO", "FATURA INTER", 4.50, "EXPENSE"),
            TxDto("SETEMBRO", "COMBUSTÍVEL", 180.00, "EXPENSE"),
            TxDto("SETEMBRO", "EMERGÊNCIA", 572.73, "EXPENSE"),
            TxDto("SETEMBRO", "LAZER", 292.97, "EXPENSE"),
            TxDto("SETEMBRO", "NUBANK", 265.45, "EXPENSE")
        )
        for (t in transactions) {
            val values = ContentValues().apply {
                put("month", t.month)
                put("category", t.category)
                put("amount", t.amount)
                put("type", t.type)
                put("description", "Lançamento inicial")
            }
            db.insert(TABLE_TRANSACTIONS, null, values)
        }

        // Initial Goals (Category, Target, Tolerance)
        val initialGoals = listOf(
            Triple("SENAI", 518.43, 50.00),
            Triple("LAZER", 600.00, 100.00),
            Triple("COMBUSTÍVEL", 510.00, 50.00),
            Triple("DÍZIMO", 300.00, 0.00),
            Triple("INVESTIMENTO", 600.00, 0.00),
            Triple("NUBANK", 226.88, 20.00),
            Triple("EMERGÊNCIA", 0.00, 100.00)
        )
        for (g in initialGoals) {
            val values = ContentValues().apply {
                put("category", g.first)
                put("target_amount", g.second)
                put("tolerance", g.third)
            }
            db.insert(TABLE_GOALS, null, values)
        }
    }

    fun validateUser(cpf: String, key: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE cpf = ? AND access_key = ?", arrayOf(cpf, key))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getTransactions(month: String): List<TransactionModel> {
        val list = mutableListOf<TransactionModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, month, category, amount, type, description FROM $TABLE_TRANSACTIONS WHERE month = ?", arrayOf(month))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TransactionModel(
                        id = cursor.getLong(0),
                        month = cursor.getString(1),
                        category = cursor.getString(2),
                        amount = cursor.getDouble(3),
                        type = cursor.getString(4),
                        description = cursor.getString(5)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getGoals(): List<GoalModel> {
        val list = mutableListOf<GoalModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, category, target_amount, tolerance FROM $TABLE_GOALS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    GoalModel(
                        id = cursor.getLong(0),
                        category = cursor.getString(1),
                        targetAmount = cursor.getDouble(2),
                        tolerance = cursor.getDouble(3)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addGoal(category: String, targetAmount: Double, tolerance: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("category", category)
            put("target_amount", targetAmount)
            put("tolerance", tolerance)
        }
        db.insert(TABLE_GOALS, null, values)
    }

    fun updateGoal(id: Long, category: String, targetAmount: Double, tolerance: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("category", category)
            put("target_amount", targetAmount)
            put("tolerance", tolerance)
        }
        db.update(TABLE_GOALS, values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteGoal(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_GOALS, "id = ?", arrayOf(id.toString()))
    }

    fun addTransaction(month: String, category: String, amount: Double, type: String, description: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("month", month)
            put("category", category)
            put("amount", amount)
            put("type", type)
            put("description", description)
        }
        db.insert(TABLE_TRANSACTIONS, null, values)
    }
}
