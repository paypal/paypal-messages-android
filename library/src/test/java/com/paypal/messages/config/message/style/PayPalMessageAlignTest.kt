package com.paypal.messages.config.message.style

import com.paypal.messages.errors.IllegalEnumArg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayPalMessageAlignTest {
	@Test
	fun testLeft() {
		assertEquals(PayPalMessageAlign.LEFT.value, 0)
		assertEquals(PayPalMessageAlign.LEFT.toString(), "LEFT")
	}

	@Test
	fun testCenter() {
		assertEquals(PayPalMessageAlign.CENTER.value, 1)
		assertEquals(PayPalMessageAlign.CENTER.toString(), "CENTER")
	}

	@Test
	fun testRight() {
		assertEquals(PayPalMessageAlign.RIGHT.value, 2)
		assertEquals(PayPalMessageAlign.RIGHT.toString(), "RIGHT")
	}

	@Test
	fun testInvalidIndex() {
		assertThrows(IllegalEnumArg::class.java) { PayPalMessageAlign(99) }
	}

	@Test
	fun testInvoke() {
		assertEquals(PayPalMessageAlign.LEFT, PayPalMessageAlign(0))
		assertEquals(PayPalMessageAlign.CENTER, PayPalMessageAlign(1))
		assertEquals(PayPalMessageAlign.RIGHT, PayPalMessageAlign(2))
	}
}
