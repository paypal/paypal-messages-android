package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductGroupTest {
	@Test
	fun testPayLater() {
		assertEquals(ProductGroup.PAY_LATER.toString(), "PAY_LATER")
	}

	@Test
	fun testPayPalCredit() {
		assertEquals(ProductGroup.PAYPAL_CREDIT.toString(), "PAYPAL_CREDIT")
	}
}
