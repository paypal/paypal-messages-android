package com.paypal.messages.config

import org.junit.Assert.assertEquals
import org.junit.Test

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
