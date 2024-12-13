package com.dokugo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ExpenseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "expenses.db"
        const val DATABASE_VERSION = 1

        // Table for expenses
        const val TABLE_NAME_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_NOTE = "note"

        // Table for predictions
        const val TABLE_NAME_PREDICTIONS = "predictions"
        const val COLUMN_PREDICTION = "prediction"
        const val COLUMN_PREDICTION_DATE = "prediction_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create table for expenses
        val createExpensesTable = ("CREATE TABLE $TABLE_NAME_EXPENSES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_AMOUNT TEXT, " +
                "$COLUMN_CATEGORY TEXT, " +
                "$COLUMN_NOTE TEXT)"
                )
        db.execSQL(createExpensesTable)

        // Create table for predictions
        val createPredictionsTable = ("CREATE TABLE $TABLE_NAME_PREDICTIONS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_PREDICTION TEXT, " +
                "$COLUMN_PREDICTION_DATE TEXT)"
                )
        db.execSQL(createPredictionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PREDICTIONS")
        onCreate(db)
    }

    // Method to insert expense data into the database
    fun insertExpense(date: String, amount: String, category: String, note: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_NOTE, note)
        }
        return db.insert(TABLE_NAME_EXPENSES, null, contentValues)
    }

    // Method to fetch all expenses and return them as a list of amounts
    fun getHistoricalExpenses(): List<Float> {
        val expenses = mutableListOf<Float>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME_EXPENSES, arrayOf(COLUMN_AMOUNT), null, null, null, null, null)

        if (cursor != null) {
            val amountColumnIndex = cursor.getColumnIndex(COLUMN_AMOUNT)

            if (amountColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    try {
                        val amount = cursor.getString(amountColumnIndex).toFloat()
                        expenses.add(amount)
                    } catch (e: Exception) {
                        Log.e("ExpenseDatabaseHelper", "Error parsing amount value: ${cursor.getString(amountColumnIndex)}", e)
                    }
                }
            } else {
                Log.e("ExpenseDatabaseHelper", "Column $COLUMN_AMOUNT not found.")
            }
            cursor.close()
        } else {
            Log.e("ExpenseDatabaseHelper", "Cursor is null.")
        }

        return expenses
    }

    // Method to fetch the latest expenses
    fun getLatestExpenses(limit: Int): List<Float> {
        val expenses = mutableListOf<Float>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_EXPENSES,
            arrayOf(COLUMN_AMOUNT),
            null, null, null, null,
            "$COLUMN_DATE DESC",
            limit.toString()
        )

        if (cursor != null) {
            val amountColumnIndex = cursor.getColumnIndex(COLUMN_AMOUNT)
            if (amountColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    try {
                        val amount = cursor.getString(amountColumnIndex).toFloat()
                        expenses.add(amount)
                    } catch (e: Exception) {
                        Log.e("ExpenseDatabaseHelper", "Error parsing amount value: ${cursor.getString(amountColumnIndex)}", e)
                    }
                }
            }
            cursor.close()
        }
        return expenses
    }

    // Method to insert prediction data into the database
    fun insertPrediction(prediction: String, date: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PREDICTION, prediction)
            put(COLUMN_PREDICTION_DATE, date)
        }
        return db.insert(TABLE_NAME_PREDICTIONS, null, contentValues)
    }

    // Method to fetch all predictions from the database
    fun getPredictions(): List<String> {
        val predictions = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME_PREDICTIONS, arrayOf(COLUMN_PREDICTION), null, null, null, null, null)

        if (cursor != null) {
            val predictionColumnIndex = cursor.getColumnIndex(COLUMN_PREDICTION)

            if (predictionColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    val prediction = cursor.getString(predictionColumnIndex)
                    predictions.add(prediction)
                }
            } else {
                Log.e("ExpenseDatabaseHelper", "Column $COLUMN_PREDICTION not found.")
            }
            cursor.close()
        } else {
            Log.e("ExpenseDatabaseHelper", "Cursor is null.")
        }

        return predictions
    }

    // Simple prediction using moving average (you can enhance this with more sophisticated algorithms)
    fun getExpensesForNext7Days(): List<Float> {
        val expenses = mutableListOf<Float>()
        val db = readableDatabase
        val query = """
        SELECT $COLUMN_AMOUNT 
        FROM $TABLE_NAME_EXPENSES 
        WHERE $COLUMN_DATE BETWEEN date('now') AND date('now', '+6 days')
        ORDER BY $COLUMN_DATE
    """
        val cursor = db.rawQuery(query, null)

        if (cursor != null) {
            val amountColumnIndex = cursor.getColumnIndex(COLUMN_AMOUNT)
            if (amountColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    try {
                        val amount = cursor.getString(amountColumnIndex).toFloat()
                        expenses.add(amount)
                    } catch (e: Exception) {
                        Log.e("ExpenseDatabaseHelper", "Error parsing amount value: ${cursor.getString(amountColumnIndex)}", e)
                    }
                }
            }
            cursor.close()
        }
        return expenses
    }

    fun getExpensesForLastMonth(): List<Float> {
        return getExpensesFromDatabase("30 days")
    }

    fun getExpensesForLastYear(): List<Float> {
        return getExpensesFromDatabase("365 days")
    }

    // Helper method for fetching data
    private fun getExpensesFromDatabase(timeRange: String): List<Float> {
        val expenses = mutableListOf<Float>()
        val db = readableDatabase
        val query = "SELECT $COLUMN_AMOUNT FROM $TABLE_NAME_EXPENSES WHERE $COLUMN_DATE >= date('now', '-$timeRange')"
        val cursor = db.rawQuery(query, null)

        if (cursor != null) {
            val amountColumnIndex = cursor.getColumnIndex(COLUMN_AMOUNT)
            if (amountColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    try {
                        val amount = cursor.getString(amountColumnIndex).toFloat()
                        expenses.add(amount)
                    } catch (e: Exception) {
                        Log.e("ExpenseDatabaseHelper", "Error parsing amount value: ${cursor.getString(amountColumnIndex)}", e)
                    }
                }
            }
            cursor.close()
        }
        return expenses
    }
}