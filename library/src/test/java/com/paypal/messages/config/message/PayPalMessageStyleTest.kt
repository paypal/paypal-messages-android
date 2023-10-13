package com.paypal.messages.config.message

import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayPalMessageStyleTest {
	@Test
	fun testConstructor() {
		val messageStyle = PayPalMessageStyle(
			logoType = PayPalMessageLogoType.ALTERNATIVE,
			color = PayPalMessageColor.MONOCHROME,
			textAlign = PayPalMessageAlign.CENTER,
		)

		assertEquals(messageStyle.logoType, PayPalMessageLogoType.ALTERNATIVE)
		assertEquals(messageStyle.color, PayPalMessageColor.MONOCHROME)
		assertEquals(messageStyle.textAlign, PayPalMessageAlign.CENTER)
	}
}
