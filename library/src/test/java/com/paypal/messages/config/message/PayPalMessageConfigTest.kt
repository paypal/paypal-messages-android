package com.paypal.messages.config.message

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

// 	@Test
	fun testSetGlobalAnalytics() {
		val name = "integration_name"
		val version = "integration_version"

// 		val mockLogger = mockk<Logger>()
// 		val mockLoggerCompanion = mockk<Logger.Companion>("mockLoggerCompanion")
// 		every { mockLoggerCompanion.getInstance(any()) } returns mockLogger
// 		every { mockLogger.setGlobalAnalytics(name, version) } answers {}

		// Create a PayPalMessageConfig object.
		val paypalMessageConfig = PayPalMessageConfig(PayPalMessageData(clientID = "1"))

		// Set the integration name and version.
		paypalMessageConfig.setGlobalAnalytics(name, version)

		// Verify that the setGlobalAnalytics() method was called on the mock Logger object.
// 		verify { mockLoggerCompanion.getInstance("") }
// 		mockLogger.setGlobalAnalytics(name, version)
// 		verify { mockLogger.setGlobalAnalytics(name, version) }
// 		assertEquals(mockLogger, mockLoggerCompanion.getInstance(""))
// 		val resultNameField = mockLogger.javaClass.getDeclaredField("integrationName")
// 		resultNameField.isAccessible = true
// 		val resultName = resultNameField.get(mockLogger)
// 		assertEquals(name, resultName)
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
}
