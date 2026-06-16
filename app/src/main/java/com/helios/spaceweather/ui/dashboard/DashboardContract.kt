package com.helios.spaceweather.ui.dashboard

import androidx.annotation.StringRes
import com.helios.spaceweather.domain.model.KpSnapshot

/**
 * MVI contract for the dashboard.
 *
 * [DashboardState] is the single immutable model the screen renders. [DashboardIntent] is the
 * closed set of user actions the ViewModel accepts. [DashboardEffect] is for one-shot events
 * (a transient message) that must not be replayed on recomposition/rotation.
 */
data class DashboardState(
    val isRefreshing: Boolean = true,
    val snapshot: KpSnapshot = KpSnapshot.EMPTY,
) {
    val hasData: Boolean get() = snapshot.current != null

    /** First-load skeleton: a refresh is in flight and there's nothing cached to show yet. */
    val isInitialLoading: Boolean get() = isRefreshing && !hasData

    /** Terminal empty state: refresh finished but the cache is still empty. */
    val isEmpty: Boolean get() = !isRefreshing && !hasData
}

sealed interface DashboardIntent {
    /** User-initiated refresh (pull-to-refresh or the toolbar action). */
    data object Refresh : DashboardIntent
}

sealed interface DashboardEffect {
    /** Show a transient message (e.g. a fetch failure that fell back to cache). */
    data class ShowMessage(@StringRes val messageRes: Int) : DashboardEffect
}
