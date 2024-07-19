package com.paypal.messages.config.modal

import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.io.Api
import com.paypal.messages.config.PayPalEnvironment as Environment

/**
 * [PayPalMessagesModalConfig] is the main configuration model for interacting with the PayPalMessage component
 * @param callbacks model with callbacks used to track the modal's display and interaction
 */
data class PayPalMessagesModalConfig(
	// These top level data fields are the only place where ID in caps is allowed
	// Everywhere else follows the camelcase convention of using "Id" since it is a "word" in code
	var clientID: String,
	var merchantID: String? = null,
	var partnerAttributionID: String? = null,
	var amount: Double? = null,
	var buyerCountry: String? = null,
	var callbacks: ModalEvents? = null,
	var offerType: PayPalMessageOfferType? = null,
	var environment: Environment = Environment.SANDBOX,
) : Cloneable {
	init {
		Api.env = environment
	}

	public override fun clone(): PayPalMessagesModalConfig {
		return super.clone() as PayPalMessagesModalConfig
	}
}
