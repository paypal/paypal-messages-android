package com.paypal.messages.config.modal

import com.paypal.messages.config.Channel
import com.paypal.messages.config.PayPalMessageOfferType

/**
 * [ModalConfig] is the main configuration model for interacting with the PayPalMessageModal component
 * @param events - [ModalEvents] model with callbacks used to track the interaction with the modal component
 */
data class ModalConfig(
	var amount: Double? = null,
	var currency: String? = null,
	var buyerCountry: String? = null,
	var offer: PayPalMessageOfferType? = null,
	var channel: Channel = Channel.NATIVE,
	var ignoreCache: Boolean = false,
	var devTouchpoint: Boolean = false,
	var stageTag: String? = null,
	var events: ModalEvents? = null,
	var modalCloseButton: ModalCloseButton,
)
