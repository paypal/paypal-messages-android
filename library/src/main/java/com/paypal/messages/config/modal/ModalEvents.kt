package com.paypal.messages.config.modal

import com.paypal.messages.errors.BaseException

/**
 * [ModalEvents] holds callbacks for tracking the interaction with a PayPalMessageModal component
 */
data class ModalEvents(
	var onClick: () -> Unit = {},
	var onApply: () -> Unit = {},
	var onLoading: () -> Unit = {},
	var onSuccess: () -> Unit = {},
	var onError: (error: BaseException) -> Unit = {},
	var onCalculate: () -> Unit = {},
	var onShow: () -> Unit = {},
	var onClose: () -> Unit = {},
)
