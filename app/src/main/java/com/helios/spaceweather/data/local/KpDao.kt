package com.helios.spaceweather.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data-access for cached Kp readings. [observeAll] is the offline-first source the UI binds to;
 * the prune queries keep the cache bounded and stop superseded forecast points from lingering.
 */
@Dao
interface KpDao {

    /** All cached readings, ascending by time — re-emits on every write. */
    @Query("SELECT * FROM kp_reading ORDER BY timestampMillis ASC")
    fun observeAll(): Flow<List<KpReadingEntity>>

    /** Insert-or-replace a batch (keyed on timestamp + forecast flag). */
    @Upsert
    suspend fun upsertAll(readings: List<KpReadingEntity>)

    /** The most recent observed reading — used by the sync worker to evaluate alerts. */
    @Query("SELECT * FROM kp_reading WHERE isForecast = 0 ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun latestObserved(): KpReadingEntity?

    /** Drop observed history older than the retention cutoff. */
    @Query("DELETE FROM kp_reading WHERE isForecast = 0 AND timestampMillis < :cutoffMillis")
    suspend fun pruneObservedBefore(cutoffMillis: Long)

    /** Drop forecast points that are now in the past (superseded predictions). */
    @Query("DELETE FROM kp_reading WHERE isForecast = 1 AND timestampMillis < :nowMillis")
    suspend fun pruneStaleForecast(nowMillis: Long)
}
