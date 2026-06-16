package com.helios.spaceweather.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helios.spaceweather.R
import com.helios.spaceweather.core.theme.HeliosTextStyles
import com.helios.spaceweather.core.theme.OnSurfaceMuted
import com.helios.spaceweather.core.theme.TrackDim
import com.helios.spaceweather.core.util.KpThreatLevel
import java.util.Locale

/**
 * A segmented arc gauge for the current Kp.
 *
 * The 270° arc is split into nine segments — one per Kp band (0→9). Segments at or below the
 * current value light up in that band's accent color (Green/Amber/Crimson via [KpThreatLevel]);
 * the rest stay dim. The hero number animates so a live update reads as movement, not a jump.
 */
@Composable
fun KpGauge(
    kp: Double,
    modifier: Modifier = Modifier,
) {
    val animatedKp by animateFloatAsState(
        targetValue = kp.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "kp",
    )
    val level = KpThreatLevel.fromKp(animatedKp.toDouble())
    val animatedAccent by animateColorAsState(
        targetValue = level.accent,
        animationSpec = tween(400),
        label = "accent",
    )

    val kpText = String.format(Locale.getDefault(), "%.1f", kp)
    val levelLabel = stringResource(level.labelRes)
    val gaugeDescription = stringResource(R.string.cd_gauge, kpText, levelLabel)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = gaugeDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * STROKE_FRACTION
            val inset = strokeWidth / 2f + size.minDimension * 0.04f
            val diameter = size.minDimension - inset * 2f
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f,
            )
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = strokeWidth)
            val segmentSweep = TOTAL_SWEEP / SEGMENT_COUNT

            for (i in 0 until SEGMENT_COUNT) {
                val segmentStart = START_ANGLE + i * segmentSweep
                // Segment i represents the band (i .. i+1]; lit once the value passes its floor.
                val isLit = animatedKp > i
                val bandAccent = KpThreatLevel.fromKp(i + 0.5).accent
                drawArc(
                    color = if (isLit) bandAccent else TrackDim,
                    startAngle = segmentStart + GAP_DEGREES / 2f,
                    sweepAngle = segmentSweep - GAP_DEGREES,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = kpText,
                style = HeliosTextStyles.GaugeValue,
                color = animatedAccent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.unit_kp),
                style = HeliosTextStyles.GaugeUnit,
                color = OnSurfaceMuted,
            )
            Text(
                text = levelLabel.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = animatedAccent.copyMuted(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Color.copyMuted(): Color = copy(alpha = 0.9f)

private const val START_ANGLE = 135f
private const val TOTAL_SWEEP = 270f
private const val SEGMENT_COUNT = 9
private const val GAP_DEGREES = 5f
private const val STROKE_FRACTION = 0.085f
