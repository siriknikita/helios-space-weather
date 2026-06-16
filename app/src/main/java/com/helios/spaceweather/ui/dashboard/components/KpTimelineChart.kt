package com.helios.spaceweather.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.spaceweather.R
import com.helios.spaceweather.core.theme.OnSurfaceMuted
import com.helios.spaceweather.core.theme.Outline
import com.helios.spaceweather.core.util.KpThreatLevel
import com.helios.spaceweather.core.util.rememberHeliosHaptics
import com.helios.spaceweather.domain.model.KpReading
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * A Compose-Canvas timeline of observed history followed by forecast.
 *
 * Each 3-hour reading is a bar scaled 0→9 and tinted by its [KpThreatLevel]; forecast bars are
 * translucent to read as "not yet real". A dashed line marks the NOAA storm threshold (Kp 5)
 * and a divider marks "now" (the observed→forecast boundary). Dragging scrubs across bars with
 * a subtle haptic tick on each new bar, and the readout above shows the selected value/time.
 */
@Composable
fun KpTimelineChart(
    history: List<KpReading>,
    forecast: List<KpReading>,
    modifier: Modifier = Modifier,
) {
    val points = remember(history, forecast) { history + forecast }
    val nowIndex = history.size
    val haptics = rememberHeliosHaptics()
    val textMeasurer = rememberTextMeasurer()

    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("EEE HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())
    }

    Column(modifier = modifier) {
        // Readout / legend row above the chart.
        val selected = selectedIndex?.let { points.getOrNull(it) }
        if (selected != null) {
            Text(
                text = stringResource(
                    R.string.timeline_readout,
                    String.format(Locale.getDefault(), "%.2f", selected.kp),
                    timeFormatter.format(selected.time),
                ),
                style = MaterialTheme.typography.labelLarge,
                color = selected.threatLevel.accent,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        } else {
            LegendRow(modifier = Modifier.padding(bottom = 8.dp))
        }

        if (points.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp))
            return@Column
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT_DP.dp)
                .pointerInput(points) {
                    detectHorizontalDragGestures(
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null },
                    ) { change, _ ->
                        val idx = indexForX(change.position.x, size.width, points.size)
                        if (idx != selectedIndex) {
                            selectedIndex = idx
                            haptics.tick()
                        }
                    }
                }
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = indexForX(offset.x, size.width, points.size)
                            haptics.tick()
                            tryAwaitRelease()
                            selectedIndex = null
                        },
                    )
                },
        ) {
            val baseline = size.height - AXIS_INSET_PX
            val top = TOP_INSET_PX
            val usableHeight = baseline - top
            val step = size.width / points.size
            val barWidth = step * BAR_WIDTH_FRACTION

            fun yFor(kp: Double): Float =
                baseline - (kp.coerceIn(0.0, KpThreatLevel.KP_MAX) / KpThreatLevel.KP_MAX).toFloat() * usableHeight

            // Storm threshold (Kp 5) dashed guide.
            val thresholdY = yFor(KpThreatLevel.STORM_THRESHOLD)
            drawLine(
                color = OnSurfaceMuted.copy(alpha = 0.4f),
                start = Offset(0f, thresholdY),
                end = Offset(size.width, thresholdY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )

            // Bars.
            points.forEachIndexed { index, reading ->
                val cx = step * index + step / 2f
                val barTop = yFor(reading.kp)
                val isSelected = index == selectedIndex
                val accent = reading.threatLevel.accent
                val color = when {
                    reading.isForecast -> accent.copy(alpha = if (isSelected) 0.75f else 0.4f)
                    else -> accent.copy(alpha = if (isSelected) 1f else 0.92f)
                }
                drawRoundRectBar(
                    left = cx - barWidth / 2f,
                    top = barTop,
                    width = barWidth,
                    bottom = baseline,
                    color = color,
                )
                if (isSelected) {
                    drawLine(
                        color = accent,
                        start = Offset(cx, top),
                        end = Offset(cx, baseline),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }

            // "Now" divider between observed and forecast.
            if (nowIndex in 1 until points.size) {
                val dividerX = step * nowIndex
                drawLine(
                    color = Outline,
                    start = Offset(dividerX, top),
                    end = Offset(dividerX, baseline),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)),
                )
                drawTimelineLabel(textMeasurer, "NOW", dividerX, top)
            }

            // Baseline.
            drawLine(
                color = Outline,
                start = Offset(0f, baseline),
                end = Offset(size.width, baseline),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

@Composable
private fun LegendRow(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.section_timeline),
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceMuted,
        modifier = modifier,
    )
}

private fun indexForX(x: Float, width: Int, count: Int): Int {
    if (count == 0 || width == 0) return 0
    val step = width.toFloat() / count
    return (x / step).roundToInt().coerceIn(0, count - 1)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectBar(
    left: Float,
    top: Float,
    width: Float,
    bottom: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    val height = (bottom - top).coerceAtLeast(2f)
    drawRect(
        color = color,
        topLeft = Offset(left, bottom - height),
        size = Size(width, height),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTimelineLabel(
    measurer: TextMeasurer,
    text: String,
    x: Float,
    y: Float,
) {
    val style = TextStyle(
        color = OnSurfaceMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp,
    )
    val measured = measurer.measure(text, style)
    drawText(
        textMeasurer = measurer,
        text = text,
        style = style,
        topLeft = Offset(
            x = (x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width),
            y = (y - measured.size.height).coerceAtLeast(0f),
        ),
    )
}

private const val CHART_HEIGHT_DP = 160
private const val BAR_WIDTH_FRACTION = 0.55f
private const val AXIS_INSET_PX = 12f
private const val TOP_INSET_PX = 18f
