package com.paypal.messages.config.message.style

import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayPalMessageLogoTypeTest {
	@Test
	fun testPrimary() {
		assertEquals(PayPalMessageLogoType.PRIMARY.value, 0)
		assertEquals(PayPalMessageLogoType.PRIMARY.toString(), "PRIMARY")
	}

	@Test
	fun testAlternative() {
		assertEquals(PayPalMessageLogoType.ALTERNATIVE.value, 1)
		assertEquals(PayPalMessageLogoType.ALTERNATIVE.toString(), "ALTERNATIVE")
	}

	@Test
	fun testInline() {
		assertEquals(PayPalMessageLogoType.INLINE.value, 2)
		assertEquals(PayPalMessageLogoType.INLINE.toString(), "INLINE")
	}

	@Test
	fun testNone() {
		assertEquals(PayPalMessageLogoType.NONE.value, 3)
		assertEquals(PayPalMessageLogoType.NONE.toString(), "NONE")
	}

	@Test
	fun testInvalidIndex() {
		assertThrows(PayPalErrors.IllegalEnumArg::class.java) { PayPalMessageLogoType(99) }
	}

	@Test
	fun testInvoke() {
		assertEquals(PayPalMessageLogoType.PRIMARY, PayPalMessageLogoType(0))
		assertEquals(PayPalMessageLogoType.ALTERNATIVE, PayPalMessageLogoType(1))
		assertEquals(PayPalMessageLogoType.INLINE, PayPalMessageLogoType(2))
		assertEquals(PayPalMessageLogoType.NONE, PayPalMessageLogoType(3))
	}
}
