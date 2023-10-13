package com.paypal.messages.config.message

import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayPalMessageDataTest {
	@Test
	fun testConstructor() {
		val data = PayPalMessageData(
			clientId = "client_id_test",
			amount = 115.0,
			placement = "placement_test",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			buyerCountry = "buyer_country_test",
			currencyCode = CurrencyCode.AUD,
			merchantID = "merchant_id_test",
			partnerAttributionID = "partner_attribution_id_test",
			environment = PayPalEnvironment.LOCAL,
		)

		assertEquals(data.clientId, "client_id_test")
		assertEquals(data.amount, 115.0)
		assertEquals(data.placement, "placement_test")
		assertEquals(data.offerType, PayPalMessageOfferType.PAY_LATER_PAY_IN_1)
		assertEquals(data.buyerCountry, "buyer_country_test")
		assertEquals(data.currencyCode, CurrencyCode.AUD)
		assertEquals(data.merchantID, "merchant_id_test")
		assertEquals(data.partnerAttributionID, "partner_attribution_id_test")
		assertEquals(data.environment, PayPalEnvironment.LOCAL)
	}
}

