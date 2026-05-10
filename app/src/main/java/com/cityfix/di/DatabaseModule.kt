package com.cityfix.di

import android.content.Context
import androidx.room.Room
import com.cityfix.data.local.dao.ReportDao
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

    @Provides
    @Singleton
    fun provideCityFixDatabase(@ApplicationContext context: Context): CityFixDatabase =
        Room.databaseBuilder(
            context,
            CityFixDatabase::class.java,
            CityFixDatabase.DATABASE_NAME
        )
            // Course project: drop & recreate on any schema change rather than maintain migrations.
            // The phone may have a higher on-disk version than the current code; this lets the
            // app come up cleanly instead of crashing with "migration from N to M not found".
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideReportDao(database: CityFixDatabase): ReportDao = database.reportDao()
}
