package com.dokugo.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createIncomeTable = "CREATE TABLE $TABLE_INCOME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_AMOUNT REAL, $COLUMN_DATE TEXT)"
        val createExpenseTable = "CREATE TABLE $TABLE_EXPENSE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_AMOUNT REAL, $COLUMN_DATE TEXT)"
        db.execSQL(createIncomeTable)
        db.execSQL(createExpenseTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSE")
        onCreate(db)
    }

    fun getIncomeData(): List<Income> {
        val incomeList = mutableListOf<Income>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_INCOME", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val amount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                incomeList.add(Income(id, amount, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return incomeList
    }

    fun getExpenseData(): List<Expense> {
        val expenseList = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSE", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val amount = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                expenseList.add(Expense(id, amount, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return expenseList
    }

    companion object {
        private const val DATABASE_NAME = "finance.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_INCOME = "income"
        const val TABLE_EXPENSE = "expense"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DATE = "date"
    }
}

data class Income(val id: Int, val amount: Float, val date: String)
data class Expense(val id: Int, val amount: Float, val date: String)