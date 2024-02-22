package com.paypal.messages.config.message

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageViewStateCallbacks] holds callbacks for tracking the process of getting PayPalMessage to display
 */
data class PayPalMessageViewStateCallbacks(
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: PayPalErrors.Base) -> Unit = {},
) : Cloneable {
	public override fun clone(): PayPalMessageViewStateCallbacks {
		return super.clone() as PayPalMessageViewStateCallbacks
	}
}
