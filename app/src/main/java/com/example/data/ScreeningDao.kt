package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreeningDao {
  @Query("SELECT * FROM screening_records ORDER BY timestamp DESC")
  fun getAllScreenings(): Flow<List<ScreeningRecord>>

  @Query("SELECT * FROM screening_records WHERE id = :id LIMIT 1")
  suspend fun getScreeningById(id: Long): ScreeningRecord?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScreening(record: ScreeningRecord): Long

  @Query("DELETE FROM screening_records WHERE id = :id")
  suspend fun deleteScreeningById(id: Long)

  @Query("DELETE FROM screening_records")
  suspend fun deleteAllScreenings()
}
