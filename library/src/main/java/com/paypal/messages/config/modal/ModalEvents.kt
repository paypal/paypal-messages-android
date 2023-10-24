package com.paypal.messages.config.modal

import com.paypal.messages.utils.PayPalErrors

/**
 * [ModalEvents] holds callbacks for tracking the interaction with a PayPalMessageModal component
 */
data class ModalEvents(
	var onClick: () -> Unit = {},
	var onApply: () -> Unit = {},
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: PayPalErrors.Base) -> Unit = {},
	var onCalculate: () -> Unit = {},
	var onShow: () -> Unit = {},
	var onClose: () -> Unit = {},
)
