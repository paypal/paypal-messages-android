package com.paypal.messages.config

import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.logger.Logger
import org.junit.Test
import io.mockk.mockk
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PayPalMessageConfigTest {
	@Test
	fun testSetGlobalAnalytics() {
		// Create a mock Logger object.
//		val mockLogger = mockk<Logger>("mockLogger")
//		whenever(mockLogger).thenReturn(mockLogger)
//
//		// Create a PayPalMessageConfig object.
//		val paypalMessageConfig = PayPalMessageConfig()
//
//		// Set the integration name and version.
//		paypalMessageConfig.setGlobalAnalytics("integration_name", "integration_version")
//
//		// Verify that the setGlobalAnalytics() method was called on the mock Logger object.
//		verify(mockLogger).setGlobalAnalytics("integration_name", "integration_version")
	}
}
