package com.helios.spaceweather.data.repository

import com.helios.spaceweather.data.local.KpDao
import com.helios.spaceweather.data.local.toDomain
import com.helios.spaceweather.data.local.toEntity
import com.helios.spaceweather.data.remote.NoaaApi
import com.helios.spaceweather.data.remote.toReadings
import com.helios.spaceweather.di.IoDispatcher
import com.helios.spaceweather.domain.model.KpReading
import com.helios.spaceweather.domain.model.KpSnapshot
import com.helios.spaceweather.domain.repository.KpRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first [KpRepository]. Room is the single source of truth: [observeSnapshot] streams
 * purely from the cache, and [refresh] fetches both NOAA products, persists them, prunes, and
 * returns the rebuilt snapshot. The network is never on the UI read path.
 */
@Singleton
class KpRepositoryImpl @Inject constructor(
    private val api: NoaaApi,
    private val dao: KpDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : KpRepository {

    override fun observeSnapshot(): Flow<KpSnapshot> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() }.toSnapshot() }

    override suspend fun refresh(): Result<KpSnapshot> = withContext(ioDispatcher) {
        runCatching {
            // The planetary-index product is observed history; the forecast product carries the
            // predicted points. Filtering each by source avoids two observed series colliding.
            val observed = api.getPlanetaryKIndex().toReadings().filterNot { it.isForecast }
            val forecast = api.getPlanetaryKForecast().toReadings().filter { it.isForecast }
            val combined = observed + forecast

            if (combined.isNotEmpty()) {
                dao.upsertAll(combined.map { it.toEntity() })
            }

            val now = Instant.now()
            dao.pruneObservedBefore(now.minus(OBSERVED_RETENTION).toEpochMilli())
            dao.pruneStaleForecast(now.toEpochMilli())

            (observed + forecast).toSnapshot()
        }
    }

    private fun List<KpReading>.toSnapshot(): KpSnapshot {
        val sorted = sortedBy { it.time }
        val history = sorted.filterNot { it.isForecast }
        val forecast = sorted.filter { it.isForecast }
        return KpSnapshot(
            current = history.lastOrNull(),
            history = history,
            forecast = forecast,
        )
    }

    companion object {
        /** How much observed history to retain locally. */
        private val OBSERVED_RETENTION: Duration = Duration.ofDays(7)
    }
}
