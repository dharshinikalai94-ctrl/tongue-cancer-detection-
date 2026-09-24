package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScreeningRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun screeningDao(): ScreeningDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "tongue_cancer_screening_db"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
