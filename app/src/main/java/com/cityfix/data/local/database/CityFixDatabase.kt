package com.cityfix.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.domain.model.Report

@Database(
    entities = [Report::class],
    version = 5,
    exportSchema = false
)
abstract class CityFixDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao

    companion object {
        const val DATABASE_NAME = "cityfix_database"
    }
}
