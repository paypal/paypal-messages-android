package com.paypal.messages.config.message

import com.paypal.messages.errors.BaseException

/**
 * [PayPalMessageViewStateCallbacks] holds callbacks for tracking the process of getting PayPalMessage to display
 */
data class PayPalMessageViewStateCallbacks(
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: BaseException) -> Unit = {},
)
