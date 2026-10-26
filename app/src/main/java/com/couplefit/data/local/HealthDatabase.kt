package com.couplefit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HealthMetricEntity::class,
        HealthDailyAggregateEntity::class,
        PartnerDailyAggregateEntity::class,
        PartnerDailyEntity::class,
        CoupleChallengeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add averageOxygenSaturation to health_daily_aggregates
                db.execSQL(
                    "ALTER TABLE health_daily_aggregates ADD COLUMN averageOxygenSaturation REAL NOT NULL DEFAULT 0.0"
                )
                // Create partner_daily_aggregates table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS partner_daily_aggregates (
                        id TEXT NOT NULL PRIMARY KEY,
                        partnerId TEXT NOT NULL,
                        dateStr TEXT NOT NULL,
                        totalSteps INTEGER NOT NULL,
                        totalActiveMinutes INTEGER NOT NULL,
                        totalCalories INTEGER NOT NULL,
                        averageHeartRate REAL NOT NULL,
                        averageOxygenSaturation REAL NOT NULL,
                        lastUpdatedTimestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create partnerDaily table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS partnerDaily (
                        id TEXT NOT NULL PRIMARY KEY,
                        partnerId TEXT NOT NULL,
                        dateStr TEXT NOT NULL,
                        steps INTEGER NOT NULL,
                        activeMinutes INTEGER NOT NULL,
                        calories INTEGER NOT NULL,
                        avgHr REAL NOT NULL,
                        avgSpo2 REAL,
                        lastUpdated INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coupleChallenges (
                        id TEXT NOT NULL PRIMARY KEY,
                        coupleId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        goal INTEGER NOT NULL,
                        start TEXT NOT NULL,
                        end TEXT NOT NULL,
                        status TEXT NOT NULL,
                        myProgress INTEGER NOT NULL,
                        partnerProgress INTEGER NOT NULL,
                        winnerUserId TEXT,
                        completedAt INTEGER,
                        lastUpdated INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}

