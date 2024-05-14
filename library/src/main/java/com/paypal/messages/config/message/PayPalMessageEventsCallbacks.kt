package com.paypal.messages.config.message

/**
 * [PayPalMessageEventsCallbacks] holds callbacks for tracking the interaction of a PayPalMessage component
 */
data class PayPalMessageEventsCallbacks(
	var onClick: () -> Unit = {},
	var onApply: () -> Unit = {},
) : Cloneable {
	public override fun clone(): PayPalMessageEventsCallbacks {
		return super.clone() as PayPalMessageEventsCallbacks
	}
}
