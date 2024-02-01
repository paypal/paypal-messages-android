package com.paypal.messages.config.message

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageViewStateCallbacks] holds callbacks for tracking the process of getting PayPalMessage to display
 */
data class PayPalMessageViewStateCallbacks(
	var onLoading: () -> Unit = empty,
	var onSuccess: () -> Unit = empty,
	var onError: (error: PayPalErrors.Base) -> Unit = emptyError,
) {
	fun merge(newCallbacks: PayPalMessageViewStateCallbacks): PayPalMessageViewStateCallbacks {
		return this.copy(
			onLoading = if (newCallbacks.onLoading != empty) newCallbacks.onLoading else empty,
			onSuccess = if (newCallbacks.onSuccess != empty) newCallbacks.onSuccess else empty,
			onError = if (newCallbacks.onError != emptyError) newCallbacks.onError else emptyError,
		)
	}
}
