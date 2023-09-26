package com.paypal.messages.config.message

import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.io.Api
import com.paypal.messages.config.PayPalEnvironment as Environment

/**
 * [PayPalMessageData] holds data used to determine the content of a PayPalMessage component
 */
data class PayPalMessageData(
	var clientId: String = "",
	var amount: Double? = null,
	var placement: String? = null,
	var offerType: PayPalMessageOfferType? = null,
	var buyerCountry: String? = null,
	var currencyCode: CurrencyCode? = null,
	// These top level data fields are the only place where ID in caps is allowed
	// Everywhere else follows the camelcase convention of using "Id" since it is a "word" in code
	var merchantID: String? = null,
	var partnerAttributionID: String? = null,
	var environment: Environment? = Environment.SANDBOX,
) {
	init {
		Api.environment = environment!!
	}
}
