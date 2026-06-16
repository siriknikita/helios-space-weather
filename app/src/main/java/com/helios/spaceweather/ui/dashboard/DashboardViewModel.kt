package com.helios.spaceweather.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.spaceweather.R
import com.helios.spaceweather.domain.repository.KpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard ViewModel (MVI).
 *
 * Offline-first: it subscribes to the repository's cached snapshot stream (which renders
 * instantly from Room) and, in parallel, kicks off a network refresh. A failed refresh emits a
 * transient message but never clears the cached data already on screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: KpRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        repository.observeSnapshot()
            .onEach { snapshot -> _state.update { it.copy(snapshot = snapshot) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            repository.refresh().onFailure {
                _effects.send(DashboardEffect.ShowMessage(R.string.error_fetch_failed))
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}
