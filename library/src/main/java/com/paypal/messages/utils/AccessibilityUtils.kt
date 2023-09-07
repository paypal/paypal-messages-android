package com.paypal.messages.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

private const val ACCESSIBILITY_DELAY = 700L

fun View.requestAccessibilityFocus() {
	Handler(Looper.getMainLooper()).postDelayed(
		{
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
		},
		ACCESSIBILITY_DELAY
	)
}

/**
 * Checks if the user has audible, spoken, visual, or braille accessibility services enabled on the device.
 *
 * @return `true` if the user has at least 1 accessibility service enabled, `false` otherwise.
 */
fun Context.isAccessibilityEnabled(): Boolean =
	(this.getSystemService(ACCESSIBILITY_SERVICE) as? AccessibilityManager)?.let {
		val types = AccessibilityServiceInfo.FEEDBACK_AUDIBLE or
			AccessibilityServiceInfo.FEEDBACK_BRAILLE or
			AccessibilityServiceInfo.FEEDBACK_SPOKEN or
			AccessibilityServiceInfo.FEEDBACK_VISUAL

		return it.getEnabledAccessibilityServiceList(types).isNotEmpty()
	} ?: false
