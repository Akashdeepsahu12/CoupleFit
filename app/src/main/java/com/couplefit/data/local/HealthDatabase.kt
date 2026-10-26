package com.couplefit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HealthMetricEntity::class, HealthDailyAggregateEntity::class], version = 1, exportSchema = false)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
}
