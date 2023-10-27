package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrencyCodeTest {
	@Test
	fun testAUD() {
		assertEquals(CurrencyCode.AUD.toString(), "AUD")
	}

	@Test
	fun testEUR() {
		assertEquals(CurrencyCode.EUR.toString(), "EUR")
	}

	@Test
	fun testGBP() {
		assertEquals(CurrencyCode.GBP.toString(), "GBP")
	}

	@Test
	fun testUSD() {
		assertEquals(CurrencyCode.USD.toString(), "USD")
	}
}
