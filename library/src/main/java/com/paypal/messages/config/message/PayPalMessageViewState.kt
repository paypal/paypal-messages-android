package com.paypal.messages.config.message

import com.paypal.messages.errors.BaseException

/**
 * [PayPalMessageViewState] holds callbacks for tracking the process of getting PayPalMessage to display
 */
data class PayPalMessageViewState(
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: BaseException) -> Unit = {},
)
