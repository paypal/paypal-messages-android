package com.paypal.messages.config.message.style

import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayPalMessageAlignmentTest {
	@Test
	fun testLeft() {
		assertEquals(PayPalMessageAlignment.LEFT.value, 0)
		assertEquals(PayPalMessageAlignment.LEFT.toString(), "LEFT")
	}

	@Test
	fun testCenter() {
		assertEquals(PayPalMessageAlignment.CENTER.value, 1)
		assertEquals(PayPalMessageAlignment.CENTER.toString(), "CENTER")
	}

	@Test
	fun testRight() {
		assertEquals(PayPalMessageAlignment.RIGHT.value, 2)
		assertEquals(PayPalMessageAlignment.RIGHT.toString(), "RIGHT")
	}

	@Test
	fun testInvalidIndex() {
		assertThrows(PayPalErrors.IllegalEnumArg::class.java) { PayPalMessageAlignment(99) }
	}

	@Test
	fun testInvoke() {
		assertEquals(PayPalMessageAlignment.LEFT, PayPalMessageAlignment(0))
		assertEquals(PayPalMessageAlignment.CENTER, PayPalMessageAlignment(1))
		assertEquals(PayPalMessageAlignment.RIGHT, PayPalMessageAlignment(2))
	}
}
