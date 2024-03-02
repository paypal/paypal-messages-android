package com.paypal.messages.logger

import com.google.gson.Gson
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackingComponentTest {
	private val offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
	private val amount = "100.00"
	private val pageType = PayPalMessagePageType.CART
	private val buyerCountryCode = "US"
	private val channel = "NATIVE"
	private val styleLogoType = PayPalMessageLogoType.ALTERNATIVE
	private val styleColor = PayPalMessageColor.MONOCHROME
	private val styleTextAlign = PayPalMessageAlign.CENTER
	private val messageType = "OFFER"
	private val views = mutableListOf("VIEW")
	private val qualifiedProducts = mutableListOf("PRODUCT")
	private val integrationIdentifier: IntegrationIdentifier? = null
	private val fdata = "test_fdata"
	private val debugId = "test_debug_id"
	private val experimentationExperienceIds = mutableListOf("EXP_1", "EXP_2")
	private val experimentationTreatmentIds = mutableListOf("TRT_1", "TRT_2")
	private val creditProductIdentifiers = mutableListOf("CPI_1", "CPI_2")
	private val offerCountryCode = "US"
	private val merchantCountryCode = "US"
	private val type = "OFFER"
	private val instanceId = "test_instance_id"
	private val originatingInstanceId = "test_originating_instance_id"
	private val sessionId = "test_session_id"
	private val componentEvents = mutableListOf(TrackingEvent(EventType.MESSAGE_CLICK))

	private val trackingComponent = TrackingComponent(
		offerType = offerType,
		amount = amount,
		pageType = pageType,
		buyerCountryCode = buyerCountryCode,
		channel = channel,
		styleLogoType = styleLogoType,
		styleColor = styleColor,
		styleTextAlign = styleTextAlign,
		messageType = messageType,
		views = views,
		qualifiedProducts = qualifiedProducts,
		integrationIdentifier = integrationIdentifier,
		fdata = fdata,
		debugId = debugId,
		experimentationExperienceIds = experimentationExperienceIds,
		experimentationTreatmentIds = experimentationTreatmentIds,
		creditProductIdentifiers = creditProductIdentifiers,
		offerCountryCode = offerCountryCode,
		merchantCountryCode = merchantCountryCode,
		type = type,
		instanceId = instanceId,
		originatingInstanceId = originatingInstanceId,
		sessionId = sessionId,
		componentEvents = componentEvents,
	)

	@Test
	fun testConstructor() {
		assertEquals(offerType, trackingComponent.offerType)
		assertEquals(amount, trackingComponent.amount)
		assertEquals(pageType, trackingComponent.pageType)
		assertEquals(buyerCountryCode, trackingComponent.buyerCountryCode)
		assertEquals(channel, trackingComponent.channel)
		assertEquals(styleLogoType, trackingComponent.styleLogoType)
		assertEquals(styleColor, trackingComponent.styleColor)
		assertEquals(styleTextAlign, trackingComponent.styleTextAlign)
		assertEquals(messageType, trackingComponent.messageType)
		assertEquals(views, trackingComponent.views)
		assertEquals(qualifiedProducts, trackingComponent.qualifiedProducts)
		assertEquals(integrationIdentifier, trackingComponent.integrationIdentifier)
		assertEquals(fdata, trackingComponent.fdata)
		assertEquals(debugId, trackingComponent.debugId)
		assertEquals(experimentationExperienceIds, trackingComponent.experimentationExperienceIds)
		assertEquals(experimentationTreatmentIds, trackingComponent.experimentationTreatmentIds)
		assertEquals(creditProductIdentifiers, trackingComponent.creditProductIdentifiers)
		assertEquals(offerCountryCode, trackingComponent.offerCountryCode)
		assertEquals(merchantCountryCode, trackingComponent.merchantCountryCode)
		assertEquals(type, trackingComponent.type)
		assertEquals(instanceId, trackingComponent.instanceId)
		assertEquals(originatingInstanceId, trackingComponent.originatingInstanceId)
		assertEquals(sessionId, trackingComponent.sessionId)
		assertEquals(componentEvents, trackingComponent.componentEvents)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(trackingComponent)

		@Suppress("ktlint:standard:max-line-length")
		val expectedJson = """{"offer_type":"PAY_LATER_SHORT_TERM","amount":"100.00","page_type":"CART","buyer_country_code":"US","channel":"NATIVE","style_logo_type":"ALTERNATIVE","style_color":"MONOCHROME","style_text_align":"CENTER","message_type":"OFFER","views":["VIEW"],"qualified_products":["PRODUCT"],"fdata":"test_fdata","debug_id":"test_debug_id","experimentation_experience_ids":["EXP_1","EXP_2"],"experimentation_treatment_ids":["TRT_1","TRT_2"],"credit_product_identifiers":["CPI_1","CPI_2"],"offer_country_code":"US","merchant_country_code":"US","type":"OFFER","instance_id":"test_instance_id","originating_instance_id":"test_originating_instance_id","session_id":"test_session_id","component_events":[{"event_type":"MESSAGE_CLICK"}],"__shared__":{}}"""
		assertEquals(expectedJson, json)
	}
}
