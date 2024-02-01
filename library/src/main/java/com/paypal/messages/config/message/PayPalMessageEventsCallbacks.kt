package com.paypal.messages.config.message

/**
 * [PayPalMessageEventsCallbacks] holds callbacks for tracking the interaction of a PayPalMessage component
 */
data class PayPalMessageEventsCallbacks(
	var onClick: () -> Unit = empty,
	var onApply: () -> Unit = empty,
) {
	fun merge(newCallbacks: PayPalMessageEventsCallbacks): PayPalMessageEventsCallbacks {
		return this.copy(
			onClick = if (newCallbacks.onClick != empty) newCallbacks.onClick else empty,
			onApply = if (newCallbacks.onApply != empty) newCallbacks.onApply else empty,
		)
	}
}
