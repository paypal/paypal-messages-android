package com.paypal.messages.io

import com.google.gson.Gson
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.modal.ModalCloseButton
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class ApiMessageDataTest {
	private val gson = Gson()
	private val fakeUuid = UUID.fromString("350e8400-e29b-41d4-a716-446655440000")

	private val creditProductGroup = ProductGroup.PAYPAL_CREDIT
	private val offerCountryCode = "US"
	private val offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
	private val messageType = "OFFER"
	private val modalCloseButton = ModalCloseButton(
		width = 100,
		height = 100,
		availableWidth = 200,
		availableHeight = 200,
		color = "#FFFFFF",
		colorType = "solid",
	)
	private val variables = ApiMessageData.Variables(logoPlaceholder = "test_logo_placeholder")
	private val merchantCountryCode = "US"
	private val creditProductIdentifiers = listOf("test_credit_product_identifier")
	private val debugId = "test_debug_id"
	private val fdata = "test_fdata"
	private val trackingKeys = listOf("test_tracking_key")
	private val originatingInstanceId = fakeUuid

	private val metadata = ApiMessageData.Metadata(
		creditProductGroup = creditProductGroup,
		offerCountryCode = offerCountryCode,
		offerType = offerType,
		messageType = messageType,
		modalCloseButton = modalCloseButton,
		variables = variables,
		merchantCountryCode = merchantCountryCode,
		creditProductIdentifiers = creditProductIdentifiers,
		debugId = debugId,
		fdata = fdata,
		trackingKeys = trackingKeys,
		originatingInstanceId = originatingInstanceId,
	)

	private val main = "test_main"
	private val disclaimer = "test_disclaimer"
	private val contentDetails = ApiMessageData.ContentDetails(main, disclaimer)
	private val contentOptions = ApiMessageData.ContentOptions(contentDetails, contentDetails)
	private val response = ApiMessageData.Response(metadata, contentOptions)

	@Test
	fun testResponseConstructor() {
		assertEquals(metadata, response.meta)
		assertEquals(contentOptions, response.content)
	}

	@Test
	fun testResponseSerialization() {
		val json = gson.toJson(response)
		assertEquals(json, "{\"meta\":{\"credit_product_group\":\"PAYPAL_CREDIT\",\"offer_country_code\":\"US\",\"offer_type\":\"PAY_LATER_SHORT_TERM\",\"message_type\":\"OFFER\",\"modal_close_button\":{\"width\":100,\"height\":100,\"available_width\":200,\"available_height\":200,\"color\":\"#FFFFFF\",\"color_type\":\"solid\"},\"variables\":{\"inline_logo_placeholder\":\"test_logo_placeholder\"},\"merchant_country_code\":\"US\",\"credit_product_identifiers\":[\"test_credit_product_identifier\"],\"debug_id\":\"test_debug_id\",\"fdata\":\"test_fdata\",\"tracking_keys\":[\"test_tracking_key\"],\"originating_instance_id\":\"350e8400-e29b-41d4-a716-446655440000\"},\"content\":{\"default\":{\"main\":\"test_main\",\"disclaimer\":\"test_disclaimer\"},\"generic\":{\"main\":\"test_main\",\"disclaimer\":\"test_disclaimer\"}}}")
	}

	@Test
	fun testContentOptionsConstructor() {
		assertEquals(contentDetails, contentOptions.default)
		assertEquals(contentDetails, contentOptions.generic)
	}

	@Test
	fun testContentOptionsSerialization() {
		val json = gson.toJson(contentOptions)
		assertEquals(json, "{\"default\":{\"main\":\"test_main\",\"disclaimer\":\"test_disclaimer\"},\"generic\":{\"main\":\"test_main\",\"disclaimer\":\"test_disclaimer\"}}")
	}

	@Test
	fun testContentDetailsConstructor() {
		assertEquals(main, contentDetails.main)
		assertEquals(disclaimer, contentDetails.disclaimer)
	}

	@Test
	fun testContentDetailsSerialization() {
		val json = gson.toJson(contentDetails)
		assertEquals(json, "{\"main\":\"test_main\",\"disclaimer\":\"test_disclaimer\"}")
	}

	@Test
	fun testMetadataConstructor() {
		assertEquals(creditProductGroup, metadata.creditProductGroup)
		assertEquals(offerCountryCode, metadata.offerCountryCode)
		assertEquals(offerType, metadata.offerType)
		assertEquals(messageType, metadata.messageType)
		assertEquals(modalCloseButton, metadata.modalCloseButton)
		assertEquals(variables, metadata.variables)
		assertEquals(merchantCountryCode, metadata.merchantCountryCode)
		assertEquals(creditProductIdentifiers, metadata.creditProductIdentifiers)
		assertEquals(debugId, metadata.debugId)
		assertEquals(fdata, metadata.fdata)
		assertEquals(trackingKeys, metadata.trackingKeys)
	}

	@Test
	fun testMetadataSerialization() {
		val json = gson.toJson(metadata)
		assertEquals(json, "{\"credit_product_group\":\"PAYPAL_CREDIT\",\"offer_country_code\":\"US\",\"offer_type\":\"PAY_LATER_SHORT_TERM\",\"message_type\":\"OFFER\",\"modal_close_button\":{\"width\":100,\"height\":100,\"available_width\":200,\"available_height\":200,\"color\":\"#FFFFFF\",\"color_type\":\"solid\"},\"variables\":{\"inline_logo_placeholder\":\"test_logo_placeholder\"},\"merchant_country_code\":\"US\",\"credit_product_identifiers\":[\"test_credit_product_identifier\"],\"debug_id\":\"test_debug_id\",\"fdata\":\"test_fdata\",\"tracking_keys\":[\"test_tracking_key\"],\"originating_instance_id\":\"350e8400-e29b-41d4-a716-446655440000\"}")
	}
}
