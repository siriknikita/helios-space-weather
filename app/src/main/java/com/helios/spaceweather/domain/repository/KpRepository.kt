package com.helios.spaceweather.domain.repository

import com.helios.spaceweather.domain.model.KpSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * The boundary between the UI/work layers and data sources.
 *
 * Offline-first: [observeSnapshot] streams from the local cache (Room) and never touches the
 * network, so the dashboard renders the last known state instantly. [refresh] performs the
 * network fetch, persists into the cache (which in turn re-emits through [observeSnapshot]),
 * and returns the freshly-built snapshot so callers (e.g. the sync worker) can inspect the
 * current Kp for alerting.
 */
interface KpRepository {

    /** Cold stream of the latest cached snapshot; emits again whenever the cache changes. */
    fun observeSnapshot(): Flow<KpSnapshot>

    /** Fetch from NOAA and persist. Returns the rebuilt snapshot, or a failure on error. */
    suspend fun refresh(): Result<KpSnapshot>
}
