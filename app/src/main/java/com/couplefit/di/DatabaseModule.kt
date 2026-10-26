package com.couplefit.di

import android.content.Context
import androidx.room.Room
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.HealthDatabase
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
    fun provideHealthDatabase(@ApplicationContext context: Context): HealthDatabase {
        return Room.databaseBuilder(
            context,
            HealthDatabase::class.java,
            "couplefit_health.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHealthDao(database: HealthDatabase): HealthDao {
        return database.healthDao()
    }
}
