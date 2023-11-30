package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.paypal.messages.config.PayPalEnvironment as Env

class PayPalEnvironmentTest {
	@Test
	fun testLive() {
		assertEquals(Env.LIVE.toString(), "LIVE")
	}

	@Test
	fun testSandbox() {
		assertEquals(Env.SANDBOX.toString(), "SANDBOX")
	}

	@Test
	fun testStage() {
		assertEquals(Env.STAGE.toString(), "STAGE")
	}

	@Test
	fun testLocal() {
		assertEquals(Env.LOCAL.toString(), "LOCAL")
	}

	@Test
	fun testIsProduction() {
		assertTrue(Env.LIVE.isProduction)
		assertTrue(Env.SANDBOX.isProduction)
		assertFalse(Env.STAGE.isProduction)
		assertFalse(Env.LOCAL.isProduction)
	}

	@Test
	fun testPresentmentUrls() {
		assertEquals(
			"http://10.0.2.2:8443/credit-presentment/native/message",
			Env.local().url(Env.Endpoints.MESSAGE_DATA).toString(),
		)
		assertEquals(
			"http://10.0.2.2:1234/credit-presentment/native/message",
			Env.local(1234).url(Env.Endpoints.MESSAGE_DATA).toString(),
		)
		assertEquals(
			"https://www.stage.host/credit-presentment/lander/modal",
			Env.stage("stage.host").url(Env.Endpoints.MODAL_DATA).toString(),
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
			"http://10.0.2.2:8443/v1/credit/upstream-messaging-events",
			Env.local().url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"http://10.0.2.2:1234/v1/credit/upstream-messaging-events",
			Env.local(1234).url(Env.Endpoints.LOGGER).toString(),
		)
		assertEquals(
			"https://api.stage.log/v1/credit/upstream-messaging-events",
			Env.stage("stage.log").url(Env.Endpoints.LOGGER).toString(),
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
