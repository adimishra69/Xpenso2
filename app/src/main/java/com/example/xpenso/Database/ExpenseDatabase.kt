package com.example.xpenso.Database


import android.content.Context
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.xpenso.BudgetDatabase.BudgetDao
import com.example.xpenso.BudgetDatabase.BudgetEntity

@Database(
    entities = [ExpenseEntity::class ,  BudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {

        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        // --- CHANGE: Added migration from version 1 to 2 ---
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

//                db.execSQL(
//                    "ALTER TABLE expenses ADD COLUMN paymentMode TEXT NOT NULL DEFAULT ''"
//                )
//
//                db.execSQL(
//                    "ALTER TABLE expenses ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'"
//                )
//
//                db.execSQL(
//                    "ALTER TABLE expenses ADD COLUMN notes TEXT NOT NULL DEFAULT ''"
//                )

            }
        }

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {

                val dbFile = context.getDatabasePath("expense_database")

//                if (!dbFile.exists()) {
//                   Toast.makeText(
//                        context.applicationContext,
//                        "🔥 FIRST TIME DB FILE CREATED",
//                        android.widget.Toast.LENGTH_LONG
//                    ).show()
//                }

//                android.widget.Toast.makeText(
//                    context.applicationContext,
//                    "🔥 Database Created",
//                    android.widget.Toast.LENGTH_SHORT
//                ).show()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .addMigrations(MIGRATION_1_2) // --- CHANGE: Registered the migration ---
                    // We can keep destructive migration as a safety fallback,
                    // but Room will now prefer the MIGRATION_1_2 logic first.
//                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
