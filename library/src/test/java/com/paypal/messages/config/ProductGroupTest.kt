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

	@Test
	fun testValueOf() {
		assertEquals(ProductGroup.PAY_LATER, ProductGroup.valueOf("PAY_LATER"))
		assertEquals(ProductGroup.PAYPAL_CREDIT, ProductGroup.valueOf("PAYPAL_CREDIT"))
	}

	@Test
	fun testValues() {
		val values = ProductGroup.values()
		assertEquals(2, values.size)
		assertEquals(ProductGroup.PAY_LATER, values[0])
		assertEquals(ProductGroup.PAYPAL_CREDIT, values[1])
	}
}
