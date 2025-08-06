package com.paypal.messagesdemo

import android.app.Application
import android.content.Context
import com.paypal.messagesdemo.utils.ApplicationContextProvider

/**
 * Custom Application class for PayPal Messages Demo.
 *
 * This ensures we have a global application context available for Toast messages
 * and other operations that require a safe context.
 */
class PayPalMessagesApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		// Initialize the application context provider
		ApplicationContextProvider.initialize(this)
	}

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)

		// Secondary initialization of application context provider
		// This ensures we have a context even if onCreate hasn't been called yet
		if (base != null) {
			ApplicationContextProvider.initialize(base)
		}
	}
}
