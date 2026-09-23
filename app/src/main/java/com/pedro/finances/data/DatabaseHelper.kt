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
    val description: String,
    val userCpf: String
)

data class GoalModel(
    val id: Long,
    val tableName: String,
    val category: String,
    val targetAmount: Double,
    val tolerance: Double,
    val userCpf: String
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "finances.db"
        private const val DATABASE_VERSION = 5

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
                name TEXT,
                email TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month TEXT,
                category TEXT,
                amount REAL,
                type TEXT,
                description TEXT,
                user_cpf TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_GOALS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                table_name TEXT,
                category TEXT,
                target_amount REAL,
                tolerance REAL,
                user_cpf TEXT
            )
        """)

        // Insert default demo user
        db.execSQL("INSERT INTO $TABLE_USERS (cpf, access_key, name, email) VALUES ('12345678900', '1234', 'Pedro User', 'pedro@email.com')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        onCreate(db)
    }

    fun validateUser(cpf: String, key: String): Boolean {
        val cleanCpf = cpf.filter { it.isDigit() }
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE cpf = ? AND access_key = ?", arrayOf(cleanCpf, key))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun userExists(cpf: String): Boolean {
        val cleanCpf = cpf.filter { it.isDigit() }
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE cpf = ?", arrayOf(cleanCpf))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserEmail(cpf: String): String? {
        val cleanCpf = cpf.filter { it.isDigit() }
        val db = readableDatabase
        var email: String? = null
        val cursor = db.rawQuery("SELECT email FROM $TABLE_USERS WHERE cpf = ?", arrayOf(cleanCpf))
        if (cursor.moveToFirst()) {
            email = cursor.getString(0)
        }
        cursor.close()
        return email
    }

    fun addUser(cpf: String, accessKey: String, name: String, email: String): Boolean {
        val cleanCpf = cpf.filter { it.isDigit() }
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("cpf", cleanCpf)
                put("access_key", accessKey)
                put("name", name)
                put("email", email)
            }
            val id = db.insert(TABLE_USERS, null, values)
            id != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun updateAccessKey(cpf: String, newKey: String): Boolean {
        val cleanCpf = cpf.filter { it.isDigit() }
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("access_key", newKey)
            }
            val rows = db.update(TABLE_USERS, values, "cpf = ?", arrayOf(cleanCpf))
            rows > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getTransactions(month: String, userCpf: String): List<TransactionModel> {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val list = mutableListOf<TransactionModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, month, category, amount, type, description, user_cpf FROM $TABLE_TRANSACTIONS WHERE month = ? AND user_cpf = ?", arrayOf(month, cleanCpf))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TransactionModel(
                        id = cursor.getLong(0),
                        month = cursor.getString(1),
                        category = cursor.getString(2),
                        amount = cursor.getDouble(3),
                        type = cursor.getString(4),
                        description = cursor.getString(5),
                        userCpf = cursor.getString(6) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllTransactions(userCpf: String): List<TransactionModel> {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val list = mutableListOf<TransactionModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, month, category, amount, type, description, user_cpf FROM $TABLE_TRANSACTIONS WHERE user_cpf = ?", arrayOf(cleanCpf))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TransactionModel(
                        id = cursor.getLong(0),
                        month = cursor.getString(1),
                        category = cursor.getString(2),
                        amount = cursor.getDouble(3),
                        type = cursor.getString(4),
                        description = cursor.getString(5),
                        userCpf = cursor.getString(6) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getGoals(userCpf: String): List<GoalModel> {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val list = mutableListOf<GoalModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, table_name, category, target_amount, tolerance, user_cpf FROM $TABLE_GOALS WHERE user_cpf = ?", arrayOf(cleanCpf))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    GoalModel(
                        id = cursor.getLong(0),
                        tableName = cursor.getString(1) ?: "GERAL",
                        category = cursor.getString(2),
                        targetAmount = cursor.getDouble(3),
                        tolerance = cursor.getDouble(4),
                        userCpf = cursor.getString(5) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addGoal(tableName: String, category: String, targetAmount: Double, tolerance: Double, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        val values = ContentValues().apply {
            put("table_name", tableName)
            put("category", category)
            put("target_amount", targetAmount)
            put("tolerance", tolerance)
            put("user_cpf", cleanCpf)
        }
        db.insert(TABLE_GOALS, null, values)
    }

    fun updateGoal(id: Long, tableName: String, category: String, targetAmount: Double, tolerance: Double, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        val values = ContentValues().apply {
            put("table_name", tableName)
            put("category", category)
            put("target_amount", targetAmount)
            put("tolerance", tolerance)
            put("user_cpf", cleanCpf)
        }
        db.update(TABLE_GOALS, values, "id = ? AND user_cpf = ?", arrayOf(id.toString(), cleanCpf))
    }

    fun deleteGoal(id: Long, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        db.delete(TABLE_GOALS, "id = ? AND user_cpf = ?", arrayOf(id.toString(), cleanCpf))
    }

    fun deleteTable(tableName: String, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        db.delete(TABLE_GOALS, "table_name = ? AND user_cpf = ?", arrayOf(tableName, cleanCpf))
    }

    fun addTransaction(month: String, category: String, amount: Double, type: String, description: String, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        val values = ContentValues().apply {
            put("month", month)
            put("category", category)
            put("amount", amount)
            put("type", type)
            put("description", description)
            put("user_cpf", cleanCpf)
        }
        db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun deleteTransaction(id: Long, userCpf: String) {
        val cleanCpf = userCpf.filter { it.isDigit() }
        val db = writableDatabase
        db.delete(TABLE_TRANSACTIONS, "id = ? AND user_cpf = ?", arrayOf(id.toString(), cleanCpf))
    }
}
