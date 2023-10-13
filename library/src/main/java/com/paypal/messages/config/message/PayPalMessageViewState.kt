package com.paypal.messages.config.message

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageViewState] holds callbacks for tracking the process of getting PayPalMessage to display
 */
data class PayPalMessageViewState(
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: PayPalErrors.Base) -> Unit = {},
)
