package com.paypal.messages.config.modal

import com.paypal.messages.config.Channel
import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.config.PayPalMessageOfferType

/**
 * [ModalConfig] is the main configuration model for interacting with the PayPalMessageModal component
 * @param events - [ModalEvents] model with callbacks used to track the interaction with the modal component
 */
data class ModalConfig(
	var amount: Double? = null,
	var buyerCountry: String? = null,
	var channel: Channel = Channel.NATIVE,
	var currencyCode: CurrencyCode? = null,
	var devTouchpoint: Boolean = false,
	var events: ModalEvents? = null,
	var ignoreCache: Boolean = false,
	var modalCloseButton: ModalCloseButton,
	var offer: PayPalMessageOfferType? = null,
	var stageTag: String? = null,
)
