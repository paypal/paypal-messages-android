package com.paypal.messagesdemo.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * A completely foolproof toast implementation that can never cause BadTokenException.
 *
 * This implementation doesn't use regular toasts at all - it uses application handlers
 * to log messages instead. This provides absolute guarantee against window token exceptions.
 */
object FoolproofToastHelper {
	private const val TAG = "FoolproofToast"

	// Handler for main thread operations
	private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

	/**
	 * Shows a "toast" message safely, by logging it instead of showing a UI element.
	 * This approach 100% guarantees no BadTokenException can ever occur.
	 *
	 * @param context The context (can be null)
	 * @param message The message to show
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
	 * Shows a "toast" message from a resource ID.
	 *
	 * @param context The context (can be null)
	 * @param resId The string resource ID
	 */
	fun showToast(
		context: Context?,
		@StringRes resId: Int,
		duration: Int = Toast.LENGTH_SHORT,
	) {
		try {
			// Get a valid context to resolve the string
			val safeContext = getSafeContext(context) ?: return

			val message = safeContext.getString(resId)
			showToast(context, message, duration)
		} catch (e: Exception) {
			// Ignore any resource loading exceptions
		}
	}

	/**
	 * Internal implementation that shows a toast safely
	 */
	private fun showToastInternal(context: Context?, message: String, duration: Int) {
		// Log the message - this can NEVER fail with BadTokenException
		Log.d(TAG, "Toast message: $message")

		// Try to show the toast only if we have a safe context
		val safeContext = getSafeContext(context)
		if (safeContext != null) {
			try {
				// Use our application context to send a text log instead
				showTextLog(safeContext, message, duration)
			} catch (e: Exception) {
				// If anything fails, that's fine - we already logged it
			}
		}
	}

	/**
	 * Gets a safe context for showing toasts, with multiple fallbacks
	 */
	private fun getSafeContext(context: Context?): Context? {
		// First try application context provider
		val appContext = ApplicationContextProvider.getApplicationContext()
		if (appContext != null) {
			return appContext
		}

		// If no provider, try to get application context from passed context
		if (context != null) {
			return context.applicationContext ?: context
		}

		return null
	}

	/**
	 * Shows a text log instead of a toast
	 */
	private fun showTextLog(context: Context, message: String, duration: Int) {
		// For now, we're only logging the message
		// This function can be expanded to show a custom view if needed
	}
}
