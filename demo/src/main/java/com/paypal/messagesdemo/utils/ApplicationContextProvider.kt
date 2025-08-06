package com.paypal.messagesdemo.utils

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Provides a safe application context that can be accessed from anywhere in the app.
 *
 * This is used as a fallback for cases where a valid context might not be available,
 * particularly for showing Toast messages from background threads or services.
 */
object ApplicationContextProvider {
	@Volatile
	private var contextRef: WeakReference<Context>? = null

	/**
	 * Initialize the provider with a context.
	 *
	 * @param context The context to use, preferably application context
	 */
	fun initialize(context: Context) {
		// Always use application context if available
		val appContext = context.applicationContext ?: context
		contextRef = WeakReference(appContext)
	}

	/**
	 * Get the application context.
	 *
	 * @return The application context, or null if not initialized
	 */
	fun getApplicationContext(): Context? {
		return contextRef?.get()
	}

	/**
	 * Check if the provider has been initialized.
	 *
	 * @return True if initialized, false otherwise
	 */
	fun isInitialized(): Boolean {
		return contextRef?.get() != null
	}
}
