package com.paypal.messages.compose

import android.content.Context
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.data.PayPalMessageDataCallback

// NOTE: Temporarily disabled Compose implementation due to compatibility issues
// When upgrading to newer Kotlin/Compose versions, this can be re-enabled

/**
 * A Jetpack Compose component that displays PayPal Messages.
 *
 * NOTE: This implementation is temporarily disabled due to compatibility issues between
 * Kotlin 1.8.22 and Compose compiler. It will be re-enabled in future versions once the
 * project is updated to a newer, compatible set of dependencies.
 *
 * For now, please use the [com.paypal.messages.PayPalMessageView] directly in your layout.
 *
 * @param config The configuration for the PayPal Message
 * @param modifier Modifier for the component (not used in this stub implementation)
 * @param onStateChange Optional callback for state changes (not used in this stub implementation)
 */
fun createPayPalMessageWithView(
	context: Context,
	config: PayPalMessageConfig,
	callback: PayPalMessageDataCallback? = null,
): PayPalMessageView {
	// Create and return a standard PayPalMessageView as fallback
	return PayPalMessageView(context, config = config)
}

/**
 * Helper to update the visibility of a PayPalMessageView based on the state.
 * This will be used when Compose support is re-enabled.
 */
fun updateMessageViewVisibility(view: PayPalMessageView, state: PayPalMessageState) {
	when (state) {
		is PayPalMessageState.Success -> view.visibility = android.view.View.VISIBLE
		PayPalMessageState.Loading -> view.visibility = android.view.View.INVISIBLE
		is PayPalMessageState.Error -> view.visibility = android.view.View.GONE
	}
}

// Note: PayPalMessageState is already defined in PayPalMessageState.kt
