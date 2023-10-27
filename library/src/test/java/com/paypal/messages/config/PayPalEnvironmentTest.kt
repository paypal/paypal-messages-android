package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayPalEnvironmentTest {
	@Test
	fun testLive() {
		assertEquals(PayPalEnvironment.LIVE.toString(), "LIVE")
	}

	@Test
	fun testSandbox() {
		assertEquals(PayPalEnvironment.SANDBOX.toString(), "SANDBOX")
	}

	@Test
	fun testStage() {
		assertEquals(PayPalEnvironment.STAGE.toString(), "STAGE")
	}

	@Test
	fun testStageVpn() {
		assertEquals(PayPalEnvironment.STAGE_VPN.toString(), "STAGE_VPN")
	}

	@Test
	fun testLocal() {
		assertEquals(PayPalEnvironment.LOCAL.toString(), "LOCAL")
	}
}
