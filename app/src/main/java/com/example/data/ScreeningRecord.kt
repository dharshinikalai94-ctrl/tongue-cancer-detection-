package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screening_records")
data class ScreeningRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val imageUri: String,
  val modelName: String = "EfficientNet-B0",
  val predictedClass: String,
  val confidenceScore: Float,
  val confidencePercentage: String,
  val explanation: String,
  val isMalignantRisk: Boolean,
  val backendSource: String = "FastAPI Backend",
  val notes: String = ""
)
