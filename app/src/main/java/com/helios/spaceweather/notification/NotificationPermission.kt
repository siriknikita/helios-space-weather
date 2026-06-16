package com.helios.spaceweather.notification

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Requests the Android 13+ `POST_NOTIFICATIONS` runtime permission exactly once per app
 * launch (the system shows its own dialog). On API < 33 this is a no-op — the permission is
 * implicitly granted. Place this once near the root of the UI.
 *
 * @param onResult invoked with whether notifications are permitted (true immediately on < 33).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission(onResult: (granted: Boolean) -> Unit = {}) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) { onResult(true) }
        return
    }

    val currentOnResult by rememberUpdatedState(onResult)
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { granted ->
        currentOnResult(granted)
    }

    // Ask only once per process; don't nag if the user already responded this launch.
    var alreadyRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> currentOnResult(true)
            !alreadyRequested -> {
                alreadyRequested = true
                permissionState.launchPermissionRequest()
            }
        }
    }
}
