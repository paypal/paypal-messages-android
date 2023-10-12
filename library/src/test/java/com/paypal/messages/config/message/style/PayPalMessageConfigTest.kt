package com.paypal.messages.config.message

import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.logger.Logger
import org.junit.Test
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals

class PayPalMessageConfigTest {
	@Test
	fun testConstructor() {
		val data = PayPalMessageData()
		val style = PayPalMessageStyle()
		val viewState = PayPalMessageViewState()
		val events = PayPalMessageEvents()

		val config = PayPalMessageConfig(
			data = data,
			style = style,
			viewState = viewState,
			events = events,
		)

		assertEquals(config.data, data)
		assertEquals(config.style, style)
		assertEquals(config.viewState, viewState)
		assertEquals(config.events, events)
	}

//	@Test
	fun testSetGlobalAnalytics() {
		val name = "integration_name"
		val version = "integration_version"

//		val mockLogger = spyk<Logger>("mockLogger")
//		val mockLoggerCompanion = mockk<Logger.Companion>("mockLoggerCompanion")
//		every { mockLoggerCompanion.getInstance(any()) } returns mockLogger
//		every { mockLogger.setGlobalAnalytics(name, version) } returns Unit

		// Create a PayPalMessageConfig object.
		val paypalMessageConfig = PayPalMessageConfig(PayPalMessageData(clientId = "1"))

		// Set the integration name and version.
		paypalMessageConfig.setGlobalAnalytics(name, version)

		// Verify that the setGlobalAnalytics() method was called on the mock Logger object.
//		verify { mockLoggerCompanion.getInstance("") }
//		mockLogger.setGlobalAnalytics(name, version)
//		verify { mockLogger.setGlobalAnalytics(name, version) }
//		assertEquals(mockLogger, mockLoggerCompanion.getInstance(""))
//		val resultNameField = mockLogger.javaClass.getDeclaredField("integrationName")
//		resultNameField.isAccessible = true
//		val resultName = resultNameField.get(mockLogger)
//		assertEquals(name, resultName)
	}
}
