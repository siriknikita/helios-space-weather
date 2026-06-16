package com.helios.spaceweather.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.spaceweather.core.theme.OnSurface
import com.helios.spaceweather.core.theme.OnSurfaceMuted
import com.helios.spaceweather.core.theme.SurfaceElevated
import com.helios.spaceweather.core.util.KpThreatLevel

/**
 * The human-readable threat panel: a status dot, the level name (+ NOAA G-scale when in storm),
 * and a one-line description. A left accent bar carries the threat color so the panel reads at a
 * glance without relying on color alone (the label and G-scale also encode severity).
 */
@Composable
fun ThreatStatusPanel(
    level: KpThreatLevel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(SurfaceElevated)
            .border(1.dp, level.accent.copy(alpha = 0.5f), MaterialTheme.shapes.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Accent spine.
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(64.dp)
                .background(level.accent),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(level.accent),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(level.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                )
                level.gScale?.let { scale ->
                    Spacer(Modifier.width(8.dp))
                    GScaleBadge(scale = scale, accent = level.accent)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(level.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun GScaleBadge(scale: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = scale,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}
