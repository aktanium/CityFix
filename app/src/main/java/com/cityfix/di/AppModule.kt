package com.cityfix.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Marks the application-wide [CoroutineScope] used for fire-and-forget work
 * that outlives any single ViewModel (e.g. background cache writes).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @AppCoroutineScope
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
