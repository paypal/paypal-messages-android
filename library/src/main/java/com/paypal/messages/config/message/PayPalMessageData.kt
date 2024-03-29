package com.paypal.messages.config.message

import com.paypal.messages.io.Api
import com.paypal.messages.config.PayPalEnvironment as Environment
import com.paypal.messages.config.PayPalMessageOfferType as OfferType
import com.paypal.messages.config.PayPalMessagePageType as PageType

/**
 * [PayPalMessageData] holds data used to determine the content of a PayPalMessage component
 */
data class PayPalMessageData(
	// These top level data fields are the only place where ID in caps is allowed
	// Everywhere else follows the camelcase convention of using "Id" since it is a "word" in code
	var clientID: String,
	var merchantID: String? = null,
	var partnerAttributionID: String? = null,
	var amount: Double? = null,
	var buyerCountry: String? = null,
	var offerType: OfferType? = null,
	var pageType: PageType? = null,
	var environment: Environment = Environment.SANDBOX,
) : Cloneable {
	init {
		Api.env = environment
	}

	public override fun clone(): PayPalMessageData {
		return super.clone() as PayPalMessageData
	}
}
