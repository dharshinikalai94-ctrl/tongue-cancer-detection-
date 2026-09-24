package com.example.data

import kotlinx.coroutines.flow.Flow

class ScreeningRepository(private val screeningDao: ScreeningDao) {
  val allScreenings: Flow<List<ScreeningRecord>> = screeningDao.getAllScreenings()

  suspend fun insertScreening(record: ScreeningRecord): Long {
    return screeningDao.insertScreening(record)
  }

  suspend fun deleteScreening(id: Long) {
    screeningDao.deleteScreeningById(id)
  }

  suspend fun clearAll() {
    screeningDao.deleteAllScreenings()
  }
}
