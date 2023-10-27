package com.paypal.messages.config.message.style

import com.paypal.messages.R
import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayPalMessageColorTest {
	@Test
	fun testBlack() {
		assertEquals(PayPalMessageColor.BLACK.value, 0)
		assertEquals(PayPalMessageColor.BLACK.colorResId, R.color.gray_700)
		assertEquals(PayPalMessageColor.BLACK.toString(), "BLACK")
	}

	@Test
	fun testWhite() {
		assertEquals(PayPalMessageColor.WHITE.value, 1)
		assertEquals(PayPalMessageColor.WHITE.colorResId, R.color.white)
		assertEquals(PayPalMessageColor.WHITE.toString(), "WHITE")
	}

	@Test
	fun testMonochrome() {
		assertEquals(PayPalMessageColor.MONOCHROME.value, 2)
		assertEquals(PayPalMessageColor.MONOCHROME.colorResId, R.color.black)
		assertEquals(PayPalMessageColor.MONOCHROME.toString(), "MONOCHROME")
	}

	@Test
	fun testGrayscale() {
		assertEquals(PayPalMessageColor.GRAYSCALE.value, 3)
		assertEquals(PayPalMessageColor.GRAYSCALE.colorResId, R.color.gray_700)
		assertEquals(PayPalMessageColor.GRAYSCALE.toString(), "GRAYSCALE")
	}

	@Test
	fun testInvalidIndex() {
		assertThrows(PayPalErrors.IllegalEnumArg::class.java) { PayPalMessageColor(99) }
	}

	@Test
	fun testInvoke() {
		assertEquals(PayPalMessageColor.BLACK, PayPalMessageColor(0))
		assertEquals(PayPalMessageColor.WHITE, PayPalMessageColor(1))
		assertEquals(PayPalMessageColor.MONOCHROME, PayPalMessageColor(2))
		assertEquals(PayPalMessageColor.GRAYSCALE, PayPalMessageColor(3))
	}
}
