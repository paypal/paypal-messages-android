package com.paypal.messages.config.message

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.paypal.messages.config.message.style.PayPalMessageAlign as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

class PayPalMessageStyleTest {
	@Test
	fun testConstructor() {
		val messageStyle = PayPalMessageStyle(
			color = Color.MONOCHROME,
			logoType = LogoType.ALTERNATIVE,
			textAlign = Align.CENTER,
		)

		assertEquals(messageStyle.color, Color.MONOCHROME)
		assertEquals(messageStyle.logoType, LogoType.ALTERNATIVE)
		assertEquals(messageStyle.textAlign, Align.CENTER)
	}

	@Test
	fun testClone() {
		val messageStyle = PayPalMessageStyle()
		val clonedMessageStyle = messageStyle.clone()

		assertEquals(messageStyle, clonedMessageStyle)
	}
}
