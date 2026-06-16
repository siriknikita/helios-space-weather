package com.helios.spaceweather.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Helios palette — a brutalist, dark-mode-first system.
 *
 * Neutrals are true-black-led and intentionally desaturated so that color carries meaning:
 * the three Kp accents ([KpGreen], [KpAmber], [KpCrimson]) are the ONLY saturated colors in
 * the app and are used strictly for data indication, never for chrome.
 */

// ---- Neutrals (surfaces & text) --------------------------------------------
/** App background. True black per the spec — also saves power on OLED. */
val TrueBlack = Color(0xFF000000)

/** Default card/surface. */
val Surface = Color(0xFF121212)

/** Elevated surface (panels stacked on a card). */
val SurfaceElevated = Color(0xFF1A1A1A)

/** Hairline borders — the brutalist outline language instead of shadows. */
val Outline = Color(0xFF262626)

/** Primary text on dark surfaces. */
val OnSurface = Color(0xFFF2F2F2)

/** Secondary/labels/captions. */
val OnSurfaceMuted = Color(0xFF8A8A8A)

// ---- Kp accents (data indication only) -------------------------------------
/** Kp 0–3 — calm geomagnetic field. */
val KpGreen = Color(0xFF2ED573)

/** Kp 4–5 — active / minor storm threshold. */
val KpAmber = Color(0xFFFFB300)

/** Kp 6+ — moderate-to-extreme geomagnetic storm. */
val KpCrimson = Color(0xFFE5383B)

/** Faint track color for the unfilled portion of gauges/charts. */
val TrackDim = Color(0xFF202020)
