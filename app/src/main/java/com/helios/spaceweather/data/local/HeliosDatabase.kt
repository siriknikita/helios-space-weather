package com.helios.spaceweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** The app's Room database. A single table backs the offline-first Kp cache. */
@Database(
    entities = [KpReadingEntity::class],
    version = 1,
    // Single-version schema; revisit (export + migrations) when the schema first changes.
    exportSchema = false,
)
abstract class HeliosDatabase : RoomDatabase() {
    abstract fun kpDao(): KpDao

    companion object {
        const val NAME = "helios.db"
    }
}
