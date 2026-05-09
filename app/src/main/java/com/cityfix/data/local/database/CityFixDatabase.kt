package com.cityfix.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.data.local.dao.UserDao
import com.cityfix.data.local.entity.UserEntity
import com.cityfix.domain.model.Report

@Database(
    entities = [Report::class, UserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class CityFixDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "cityfix_database"
    }
}

