package com.paypal.messagesdemo.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * A completely safe helper class to show Toast messages without BadTokenException.
 *
 * This implementation:
 * - Always uses application context
 * - Checks for destroyed/finishing activities
 * - Uses a singleton toast instance to avoid multiple toasts
 * - Shows toasts on the main thread
 * - Has multiple fallback mechanisms
 * - Has full exception handling
 * - Manages toast lifecycle to prevent leaked windows
 */
object ToastHelper {
	// Single toast instance to prevent multiple toasts stacking
	private var toast: Toast? = null

	// Handler for main thread operations
	private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

	/**
	 * Safely shows a toast message.
	 *
	 * @param context The context
	 * @param message The message to show
	 * @param duration Toast duration (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
	 */
	fun showToast(context: Context?, message: String?, duration: Int = Toast.LENGTH_SHORT) {
		if (message == null) return

		// Always run on main thread
		if (Looper.myLooper() != Looper.getMainLooper()) {
			mainHandler.post { showToastInternal(context, message, duration) }
		} else {
			showToastInternal(context, message, duration)
		}
	}

	/**
	 * Safely shows a toast message from a resource.
	 *
	 * @param context The context
	 * @param resId The string resource ID
	 * @param duration Toast duration (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
	 */
	fun showToast(
		context: Context?,
		@StringRes resId: Int,
		duration: Int = Toast.LENGTH_SHORT,
	) {
		try {
			if (context == null) {
				// Try using application context provider as fallback
				val appContext = ApplicationContextProvider.getApplicationContext()
				if (appContext != null) {
					val message = appContext.getString(resId)
					showToast(appContext, message, duration)
				}
				return
			}

			val message = context.getString(resId)
			showToast(context, message, duration)
		} catch (e: Exception) {
			// Ignore any exceptions when getting the string
		}
	}

	/**
	 * Internal implementation that actually shows the toast
	 */
	private fun showToastInternal(context: Context?, message: String, duration: Int) {
		try {
			// Get the safest possible context
			val safeContext = getSafeContext(context)
			if (safeContext == null) {
				// If we still can't get a safe context, don't show toast
				return
			}

			// Cancel any existing toast to prevent stacking
			cancelCurrentToast()

			// Create and show new toast
			toast = Toast.makeText(safeContext, message, duration)
			toast?.show()
		} catch (e: Exception) {
			// Completely ignore any toast exceptions
		}
	}

	/**
	 * Cancels the current toast if any
	 */
	private fun cancelCurrentToast() {
		try {
			toast?.cancel()
		} catch (e: Exception) {
			// Ignore
		}
	}

	/**
	 * Gets the safest context possible, with multiple fallbacks
	 */
	private fun getSafeContext(context: Context?): Context? {
		if (context == null) {
			// Try application context provider
			return ApplicationContextProvider.getApplicationContext()
		}

		// Check if context is an activity that's finishing or destroyed
		if (context is Activity) {
			if (context.isFinishing || context.isDestroyed) {
				// Activity is not valid, try application context
				return context.applicationContext ?: ApplicationContextProvider.getApplicationContext()
			}
		}

		// Always prefer application context if available
		return when {
			context is Application -> context
			context.applicationContext != null -> context.applicationContext
			else -> ApplicationContextProvider.getApplicationContext() ?: context
		}
	}
}
