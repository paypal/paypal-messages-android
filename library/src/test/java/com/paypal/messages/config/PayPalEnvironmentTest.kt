package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.paypal.messages.config.PayPalEnvironment as Env

class PayPalEnvironmentTest {
	@Test
	fun testIsProduction() {
		assertTrue(Env.LIVE.isProduction, "LIVE is not production")
		assertTrue(Env.SANDBOX.isProduction, "SANDBOX is not production")
		assertFalse(Env.STAGE("").isProduction, "STAGE is production")
		assertFalse(Env.LOCAL().isProduction, "LOCAL is production")
	}

	@Test
	fun testPresentmentUrls() {
		assertEquals(
			"http://localhost:8443/credit-presentment/native/message",
			Env.LOCAL().url(Env.Endpoints.MESSAGE_DATA).toString(),
		)
		assertEquals(
			"http://localhost:1234/credit-presentment/native/message",
			Env.LOCAL(1234).url(Env.Endpoints.MESSAGE_DATA).toString(),
		)
		assertEquals(
			"https://www.stage.host/credit-presentment/lander/modal",
			Env.STAGE("stage.host").url(Env.Endpoints.MODAL_DATA).toString(),
		)
		assertEquals(
			"https://www.sandbox.paypal.com/credit-presentment/merchant-profile",
			Env.SANDBOX.url(Env.Endpoints.MERCHANT_PROFILE).toString(),
		)
		assertEquals(
			"https://www.paypal.com/credit-presentment/native/message",
			Env.LIVE.url(Env.Endpoints.MESSAGE_DATA).toString(),
		)
	}

	@Test
	fun testLoggerUrls() {
		assertEquals(
			"http://localhost:8443/v1/credit/upstream-messaging-events",
			Env.LOCAL().url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"http://localhost:1234/v1/credit/upstream-messaging-events",
			Env.LOCAL(1234).url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"https://api.stage.log/v1/credit/upstream-messaging-events",
			Env.STAGE("stage.log").url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"https://api.sandbox.paypal.com/v1/credit/upstream-messaging-events",
			Env.SANDBOX.url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"https://api.paypal.com/v1/credit/upstream-messaging-events",
			Env.LIVE.url(Env.Endpoints.LOGGER).toString(),
		)
	}
}
