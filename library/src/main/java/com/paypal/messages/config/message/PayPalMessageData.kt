package com.paypal.messages.config.message

import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.io.Api
import com.paypal.messages.config.PayPalEnvironment as Environment
import com.paypal.messages.config.PayPalMessageOfferType as OfferType

/**
 * [PayPalMessageData] holds data used to determine the content of a PayPalMessage component
 */
data class PayPalMessageData(
	// These top level data fields are the only place where ID in caps is allowed
	// Everywhere else follows the camelcase convention of using "Id" since it is a "word" in code
	var clientID: String = "",
	var merchantID: String? = null,
	var partnerAttributionID: String? = null,
	var amount: Double? = null,
	var buyerCountry: String? = null,
	var currencyCode: CurrencyCode? = null,
	var offerType: OfferType? = null,
	var placement: String? = null,
	var environment: Environment? = null,
) {
	init {
		Api.environment = environment ?: Environment.SANDBOX
	}

	fun merge(newData: PayPalMessageData): PayPalMessageData {
		return this.copy(
			clientID = if (newData.clientID != "") newData.clientID else this.clientID,
			merchantID = newData.merchantID ?: this.merchantID,
			partnerAttributionID = newData.partnerAttributionID ?: this.partnerAttributionID,
			amount = newData.amount ?: this.amount,
			buyerCountry = newData.buyerCountry ?: this.buyerCountry,
			currencyCode = newData.currencyCode ?: this.currencyCode,
			offerType = newData.offerType ?: this.offerType,
			placement = newData.placement ?: this.placement,
			environment = newData.environment ?: this.environment,
		)
	}
}
