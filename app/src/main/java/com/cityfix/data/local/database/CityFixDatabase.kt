package com.cityfix.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.data.local.entity.ReportEntity

@Database(
    entities = [ReportEntity::class],
    version = 8,
    exportSchema = false
)
abstract class CityFixDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao

    companion object {
        const val DATABASE_NAME = "cityfix_database"
    }
}
