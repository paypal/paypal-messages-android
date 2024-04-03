package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GlobalAnalyticsTest {
	@Test
	fun testGlobalAnalyticsUnset() {
		assertEquals("", GlobalAnalytics.integrationName)
		assertEquals("", GlobalAnalytics.integrationVersion)
		assertEquals("", GlobalAnalytics.deviceId)
		assertEquals("", GlobalAnalytics.sessionId)
	}
}
