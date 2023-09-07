package com.paypal.messages.config.message

/**
 * [PayPalMessageEvents] holds callbacks for tracking the interaction of a PayPalMessage component
 */
data class PayPalMessageEvents(
	var onClick: () -> Unit = {},
	var onApply: () -> Unit = {},
)
