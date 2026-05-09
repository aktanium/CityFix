package com.cityfix.di

import com.cityfix.data.repository.AuthRepositoryImpl
import com.cityfix.data.repository.ReportRepositoryImpl
import com.cityfix.data.repository.SettingsRepositoryImpl
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.repository.ReportRepository
import com.cityfix.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
