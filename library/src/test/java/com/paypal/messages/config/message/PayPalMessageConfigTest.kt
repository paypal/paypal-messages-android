package com.paypal.messages.config.message

import com.paypal.messages.analytics.GlobalAnalytics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PayPalMessageConfigTest {
	@Test
	fun testConstructor() {
		val data = PayPalMessageData(clientID = "1")
		val style = PayPalMessageStyle()
		val viewStateCallbacks = PayPalMessageViewStateCallbacks()
		val eventsCallbacks = PayPalMessageEventsCallbacks()

		val config = PayPalMessageConfig(
			data = data,
			style = style,
			viewStateCallbacks = viewStateCallbacks,
			eventsCallbacks = eventsCallbacks,
		)

		assertEquals(config.data, data)
		assertEquals(config.style, style)
		assertEquals(config.viewStateCallbacks, viewStateCallbacks)
		assertEquals(config.eventsCallbacks, eventsCallbacks)
	}

	@Test
	fun testSetGlobalAnalytics() {
		val name = "integration_name"
		val version = "integration_version"

		assertEquals("", GlobalAnalytics.integrationName)
		assertEquals("", GlobalAnalytics.integrationVersion)

		PayPalMessageConfig.setGlobalAnalytics(name, version)

		assertEquals(name, GlobalAnalytics.integrationName)
		assertEquals(version, GlobalAnalytics.integrationVersion)
	}

	// This tests if PayPalMessageConfig sets GlobalAnalytics to empty strings when used
	// It is also required for full test coverage as calculated by kover
	@Test
	fun testSetGlobalAnalyticsWithNoValues() {
		PayPalMessageConfig.setGlobalAnalytics("", "")

		assertEquals("", GlobalAnalytics.integrationName)
		assertEquals("", GlobalAnalytics.integrationVersion)
	}

	@Test
	fun testClone() {
		val data = PayPalMessageData(clientID = "1")
		val style = PayPalMessageStyle()
		val viewStateCallbacks = PayPalMessageViewStateCallbacks()
		val eventsCallbacks = PayPalMessageEventsCallbacks()

		val config = PayPalMessageConfig(
			data = data,
			style = style,
			viewStateCallbacks = viewStateCallbacks,
			eventsCallbacks = eventsCallbacks,
		)

		val cloneConfig = config.clone()

		assertEquals(config, cloneConfig)
		config.data = PayPalMessageData(clientID = "2")
		assertNotEquals(config, cloneConfig)
	}

	@Test
	fun testCloneWithNoCallbacks() {
		val data = PayPalMessageData(clientID = "1")
		val style = PayPalMessageStyle()

		val config = PayPalMessageConfig(
			data = data,
			style = style,
		)

		val cloneConfig = config.clone()

		assertEquals(config, cloneConfig)
		config.data = PayPalMessageData(clientID = "2")
		assertNotEquals(config, cloneConfig)
	}
}
