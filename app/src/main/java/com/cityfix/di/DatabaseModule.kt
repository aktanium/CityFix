package com.cityfix.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.data.local.dao.UserDao
import com.cityfix.data.local.database.CityFixDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `passwordHash` TEXT NOT NULL,
                    `passwordSalt` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideCityFixDatabase(@ApplicationContext context: Context): CityFixDatabase =
        Room.databaseBuilder(
            context,
            CityFixDatabase::class.java,
            CityFixDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideReportDao(database: CityFixDatabase): ReportDao = database.reportDao()

    @Provides
    fun provideUserDao(database: CityFixDatabase): UserDao = database.userDao()
}
