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
			clientID = "client_id_test",
			amount = 115.0,
			placement = "placement_test",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			buyerCountry = "buyer_country_test",
			currencyCode = CurrencyCode.AUD,
			merchantID = "merchant_id_test",
			partnerAttributionID = "partner_attribution_id_test",
			environment = PayPalEnvironment.LOCAL,
		)

		assertEquals(data.clientID, "client_id_test")
		assertEquals(data.amount, 115.0)
		assertEquals(data.placement, "placement_test")
		assertEquals(data.offerType, PayPalMessageOfferType.PAY_LATER_PAY_IN_1)
		assertEquals(data.buyerCountry, "buyer_country_test")
		assertEquals(data.currencyCode, CurrencyCode.AUD)
		assertEquals(data.merchantID, "merchant_id_test")
		assertEquals(data.partnerAttributionID, "partner_attribution_id_test")
		assertEquals(data.environment, PayPalEnvironment.LOCAL)
	}

	val oldData = PayPalMessageData(
		clientID = "test_client_id",
		merchantID = "test_merchant_id",
		partnerAttributionID = "test_partner_attribution_id",
		amount = 115.0,
		buyerCountry = "ES",
		currencyCode = CurrencyCode.EUR,
		offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
		placement = "test_placement",
		environment = PayPalEnvironment.LOCAL,
	)

	@Test
	fun testMergeWithNoParams() {
		val expectedData = oldData.copy()
		val actualData = oldData.merge(PayPalMessageData())
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
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualClientIdData = oldData.merge(PayPalMessageData(clientID = "new_client_id"))
		assertEquals(expectedClientIdData, actualClientIdData)

		val expectedMerchantIdData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "new_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualMerchantIdData = oldData.merge(PayPalMessageData(merchantID = "new_merchant_id"))
		assertEquals(expectedMerchantIdData, actualMerchantIdData)
		
		val expectedPartnerAttributionIdData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "new_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualPartnerAttributionIdData = oldData.merge(
			PayPalMessageData(partnerAttributionID = "new_partner_attribution_id"),
		)
		assertEquals(expectedPartnerAttributionIdData, actualPartnerAttributionIdData)
		
		val expectedAmountData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 1.15,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualAmountData = oldData.merge(PayPalMessageData(amount = 1.15))
		assertEquals(expectedAmountData, actualAmountData)

		val expectedBuyerCountryData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "US",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualBuyerCountryData = oldData.merge(PayPalMessageData(buyerCountry = "US"))
		assertEquals(expectedBuyerCountryData, actualBuyerCountryData)

		val expectedCurrencyCodeData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.AUD,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualCurrencyCodeData = oldData.merge(PayPalMessageData(currencyCode = CurrencyCode.AUD))
		assertEquals(expectedCurrencyCodeData, actualCurrencyCodeData)

		val expectedOfferTypeData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualOfferTypeData = oldData.merge(
			PayPalMessageData(offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST),
		)
		assertEquals(expectedOfferTypeData, actualOfferTypeData)

		val expectedPlacementData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "new_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualPlacementData = oldData.merge(PayPalMessageData(placement = "new_placement"))
		assertEquals(expectedPlacementData, actualPlacementData)

		val expectedEnvironmentData = PayPalMessageData(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_attribution_id",
			amount = 115.0,
			buyerCountry = "ES",
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.STAGE,
		)
		val actualEnvironmentData = oldData.merge(PayPalMessageData(environment = PayPalEnvironment.STAGE))
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
			currencyCode = CurrencyCode.EUR,
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			placement = "test_placement",
			environment = PayPalEnvironment.LOCAL,
		)
		val actualData = oldData.merge(expectedData)
		assertEquals(expectedData, actualData)
	}
}
