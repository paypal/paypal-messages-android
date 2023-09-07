package com.paypal.messages.extensions

import android.content.Context
import android.util.DisplayMetrics

/**
 * Returns the device display density in DIP
 */
fun Context.getDisplayDensityInDp(): Int = resources.displayMetrics.densityDpi

/**
 * Returns the device display density name. Possible names are:
 * LDPI, MDPI, HDPI, XHDPI, XXHDPI, XXXHDPI, or N/A if the display isn't reporting its density
 */
fun Context.getDisplayDensityName(): String = when (getDisplayDensityInDp()) {
	DisplayMetrics.DENSITY_LOW,
	DisplayMetrics.DENSITY_140,
	-> "LDPI"

	DisplayMetrics.DENSITY_MEDIUM,
	DisplayMetrics.DENSITY_180,
	DisplayMetrics.DENSITY_200,
	-> "MDPI"

	DisplayMetrics.DENSITY_TV,
	DisplayMetrics.DENSITY_HIGH,
	DisplayMetrics.DENSITY_220,
	DisplayMetrics.DENSITY_260,
	-> "HDPI"

	DisplayMetrics.DENSITY_XHIGH,
	DisplayMetrics.DENSITY_280,
	DisplayMetrics.DENSITY_300,
	DisplayMetrics.DENSITY_340,
	-> "XHDPI"

	DisplayMetrics.DENSITY_XXHIGH,
	DisplayMetrics.DENSITY_360,
	DisplayMetrics.DENSITY_400,
	DisplayMetrics.DENSITY_420,
	-> "XXHDPI"

	DisplayMetrics.DENSITY_XXXHIGH,
	DisplayMetrics.DENSITY_560,
	DisplayMetrics.DENSITY_600,
	-> "XXXHDPI"

	else -> "N/A"
}
