package com.paypal.messages.config.message

import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayPalMessageDataTest {
	private val initialClientID = "test_client_id"
	
	@Test
	fun testConstructor() {
		val data = PayPalMessageData(
			clientID = initialClientID,
			amount = 115.0,
			placement = "placement_test",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			buyerCountry = "buyer_country_test",
			merchantID = "merchant_id_test",
			partnerAttributionID = "partner_attribution_id_test",
			environment = PayPalEnvironment.LOCAL,
		)

		assertEquals(data.clientID, initialClientID)
		assertEquals(data.amount, 115.0)
		assertEquals(data.placement, "placement_test")
		assertEquals(data.offerType, PayPalMessageOfferType.PAY_LATER_PAY_IN_1)
		assertEquals(data.buyerCountry, "buyer_country_test")
		assertEquals(data.merchantID, "merchant_id_test")
		assertEquals(data.partnerAttributionID, "partner_attribution_id_test")
		assertEquals(data.environment, PayPalEnvironment.LOCAL)
	}

	private val oldData = PayPalMessageData(
		clientID = initialClientID,
		merchantID = "test_merchant_id",
		partnerAttributionID = "test_partner_attribution_id",
		amount = 115.0,
		buyerCountry = "ES",
		offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
		placement = "test_placement",
		environment = PayPalEnvironment.LOCAL,
	)

	@Test
	fun testMergeWithNoParams() {
		val expectedData = oldData.copy()
		val actualData = oldData.merge(PayPalMessageData(initialClientID))
		assertEquals(expectedData, actualData)
	}

	@Test
	fun testMergeWithOneParam() {
		val expectedClientIdData = PayPalMessageData(
			clientID = "new_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualClientIdData = oldData.merge(PayPalMessageData(clientID = "new_client_id"))
		assertEquals(expectedClientIdData, actualClientIdData)

		val expectedMerchantIdData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "new_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualMerchantIdData = oldData.merge(PayPalMessageData(initialClientID, merchantID = "new_merchant_id"))
		assertEquals(expectedMerchantIdData, actualMerchantIdData)
		
		val expectedPartnerAttributionIdData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "new_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualPartnerAttributionIdData = oldData.merge(
			PayPalMessageData(initialClientID, partnerAttributionID = "new_partner_attribution_id"),
		)
		assertEquals(expectedPartnerAttributionIdData, actualPartnerAttributionIdData)
		
		val expectedAmountData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 1.15,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualAmountData = oldData.merge(PayPalMessageData(initialClientID, amount = 1.15))
		assertEquals(expectedAmountData, actualAmountData)

		val expectedBuyerCountryData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "US",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualBuyerCountryData = oldData.merge(PayPalMessageData(initialClientID, buyerCountry = "US"))
		assertEquals(expectedBuyerCountryData, actualBuyerCountryData)

		val expectedOfferTypeData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualOfferTypeData = oldData.merge(
			PayPalMessageData(initialClientID, offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST),
		)
		assertEquals(expectedOfferTypeData, actualOfferTypeData)

		val expectedPlacementData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "new_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualPlacementData = oldData.merge(PayPalMessageData(initialClientID, placement = "new_placement"))
		assertEquals(expectedPlacementData, actualPlacementData)

		val expectedEnvironmentData = PayPalMessageData(
			clientID = initialClientID,
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.STAGE,
		)
		val actualEnvironmentData = oldData.merge(PayPalMessageData(initialClientID, environment = PayPalEnvironment.STAGE))
		assertEquals(expectedEnvironmentData, actualEnvironmentData)
	}

	@Test
	fun testMergeWithAllParams() {
		val expectedData = PayPalMessageData(
			clientID = "new_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualData = oldData.merge(expectedData)
		assertEquals(expectedData, actualData)
	}
}
