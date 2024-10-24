package com.paypal.messages.analytics

import com.google.gson.Gson
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AnalyticsComponentTest {
	private val offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
	private val amount = "100.00"
	private val pageType = PayPalMessagePageType.CART
	private val buyerCountryCode = "US"
	private val channel = "UPSTREAM"
	private val styleLogoType = PayPalMessageLogoType.ALTERNATIVE
	private val styleColor = PayPalMessageColor.MONOCHROME
	private val styleTextAlign = PayPalMessageAlignment.CENTER
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
	private val componentEvents = mutableListOf(AnalyticsEvent(EventType.MESSAGE_CLICKED))

	private val analyticsComponent = AnalyticsComponent(
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
		componentEvents = componentEvents,
	)

	@Test
	fun testConstructor() {
		assertEquals(offerType, analyticsComponent.offerType)
		assertEquals(amount, analyticsComponent.amount)
		assertEquals(pageType, analyticsComponent.pageType)
		assertEquals(buyerCountryCode, analyticsComponent.buyerCountryCode)
		assertEquals(channel, analyticsComponent.channel)
		assertEquals(styleLogoType, analyticsComponent.styleLogoType)
		assertEquals(styleColor, analyticsComponent.styleColor)
		assertEquals(styleTextAlign, analyticsComponent.styleTextAlign)
		assertEquals(messageType, analyticsComponent.messageType)
		assertEquals(views, analyticsComponent.views)
		assertEquals(qualifiedProducts, analyticsComponent.qualifiedProducts)
		assertEquals(integrationIdentifier, analyticsComponent.integrationIdentifier)
		assertEquals(fdata, analyticsComponent.fdata)
		assertEquals(debugId, analyticsComponent.debugId)
		assertEquals(experimentationExperienceIds, analyticsComponent.experimentationExperienceIds)
		assertEquals(experimentationTreatmentIds, analyticsComponent.experimentationTreatmentIds)
		assertEquals(creditProductIdentifiers, analyticsComponent.creditProductIdentifiers)
		assertEquals(offerCountryCode, analyticsComponent.offerCountryCode)
		assertEquals(merchantCountryCode, analyticsComponent.merchantCountryCode)
		assertEquals(type, analyticsComponent.type)
		assertEquals(instanceId, analyticsComponent.instanceId)
		assertEquals(originatingInstanceId, analyticsComponent.originatingInstanceId)
		assertEquals(componentEvents, analyticsComponent.componentEvents)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(analyticsComponent)

		val expectedParts = arrayOf(
			""""offer_type":"PAY_LATER_SHORT_TERM"""",
			""""amount":"100.00"""",
			""""page_type":"CART"""",
			""""buyer_country_code":"US"""",
			""""presentment_channel":"UPSTREAM"""",
			""""style_logo_type":"alternative"""",
			""""style_color":"monochrome"""",
			""""style_text_align":"center"""",
			""""message_type":"OFFER"""",
			""""views":["VIEW"]""",
			""""qualified_products":["PRODUCT"]""",
			""""fdata":"test_fdata"""",
			""""debug_id":"test_debug_id"""",
			""""experimentation_experience_ids":["EXP_1","EXP_2"]""",
			""""experimentation_treatment_ids":["TRT_1","TRT_2"]""",
			""""credit_product_identifiers":["CPI_1","CPI_2"]""",
			""""offer_country_code":"US"""",
			""""merchant_country_code":"US"""",
			""""type":"OFFER"""",
			""""instance_id":"test_instance_id"""",
			""""originating_instance_id":"test_originating_instance_id"""",
			""""component_events":[{"event_type":"message_clicked"}]""",
			""""__shared__":{}""",
		)
		expectedParts.forEach { assertTrue(it in json, "json does not contain $it") }
	}
}
