package com.helios.spaceweather.ui.dashboard

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.spaceweather.R
import com.helios.spaceweather.core.theme.OnSurfaceMuted
import com.helios.spaceweather.core.theme.TrueBlack
import com.helios.spaceweather.domain.model.KpSnapshot
import com.helios.spaceweather.ui.dashboard.components.KpGauge
import com.helios.spaceweather.ui.dashboard.components.KpTimelineChart
import com.helios.spaceweather.ui.dashboard.components.ThreatStatusPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = TrueBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.dashboard_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceMuted,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(DashboardIntent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.cd_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrueBlack),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onIntent(DashboardIntent.Refresh) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when {
                state.isInitialLoading -> LoadingContent()
                state.isEmpty -> EmptyContent(onRetry = { viewModel.onIntent(DashboardIntent.Refresh) })
                else -> DashboardContent(snapshot = state.snapshot)
            }
        }
    }
}

@Composable
private fun DashboardContent(snapshot: KpSnapshot) {
    val current = snapshot.current ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UpdatedLabel(snapshot)
        Spacer(Modifier.height(8.dp))
        KpGauge(
            kp = current.kp,
            modifier = Modifier.fillMaxWidth(0.72f),
        )
        Spacer(Modifier.height(20.dp))
        ThreatStatusPanel(level = current.threatLevel)
        Spacer(Modifier.height(28.dp))
        KpTimelineChart(
            history = snapshot.history,
            forecast = snapshot.forecast,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun UpdatedLabel(snapshot: KpSnapshot) {
    val updated = snapshot.lastUpdated
    val text = if (updated != null) {
        val relative = DateUtils.getRelativeTimeSpanString(
            updated.toEpochMilli(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
        stringResource(R.string.last_updated, relative)
    } else {
        stringResource(R.string.last_updated_never)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceMuted,
    )
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OnSurfaceMuted, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.status_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun EmptyContent(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.empty_no_data),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}
