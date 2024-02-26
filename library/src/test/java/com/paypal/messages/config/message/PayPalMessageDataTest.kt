package com.paypal.messages.config.message

import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PayPalMessageDataTest {
	private val initialClientID = "test_client_id"

	@Test
	fun testConstructor() {
		val data = PayPalMessageData(
			clientID = initialClientID,
			amount = 115.0,
			pageType = PayPalMessagePageType.CART,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			buyerCountry = "buyer_country_test",
			merchantID = "merchant_id_test",
			partnerAttributionID = "partner_attribution_id_test",
			environment = PayPalEnvironment.LOCAL,
		)

		assertEquals(data.clientID, initialClientID)
		assertEquals(data.amount, 115.0)
		assertEquals(data.pageType, PayPalMessagePageType.CART)
		assertEquals(data.offerType, PayPalMessageOfferType.PAY_LATER_PAY_IN_1)
		assertEquals(data.buyerCountry, "buyer_country_test")
		assertEquals(data.merchantID, "merchant_id_test")
		assertEquals(data.partnerAttributionID, "partner_attribution_id_test")
		assertEquals(data.environment, PayPalEnvironment.LOCAL)
	}

	@Test
	fun testClone() {
		val oldData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			pageType = PayPalMessagePageType.CART,
			environment = PayPalEnvironment.LOCAL,
		)

		val data = oldData.clone()
		assertEquals(data, oldData)
		data.amount = 100.00
		assertNotEquals(oldData, data)
	}
}
