package com.paypal.messages.config.message

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.paypal.messages.config.message.style.PayPalMessageAlignment as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

class PayPalMessageStyleTest {
	@Test
	fun testConstructor() {
		val messageStyle = PayPalMessageStyle(
			color = Color.MONOCHROME,
			logoType = LogoType.ALTERNATIVE,
			textAlignment = Align.CENTER,
		)

		assertEquals(messageStyle.color, Color.MONOCHROME)
		assertEquals(messageStyle.logoType, LogoType.ALTERNATIVE)
		assertEquals(messageStyle.textAlignment, Align.CENTER)
	}

	@Test
	fun testClone() {
		val messageStyle = PayPalMessageStyle()
		val clonedMessageStyle = messageStyle.clone()

		assertEquals(messageStyle, clonedMessageStyle)
	}

	@Test
	fun testDefaultValues() {
		val messageStyle = PayPalMessageStyle()
		
		assertEquals(Color.BLACK, messageStyle.color)
		assertEquals(LogoType.PRIMARY, messageStyle.logoType)
		assertEquals(Align.LEFT, messageStyle.textAlignment)
	}

	@Test
	fun testEquality() {
		val style1 = PayPalMessageStyle(
			color = Color.WHITE,
			logoType = LogoType.INLINE,
			textAlignment = Align.RIGHT,
		)
		val style2 = PayPalMessageStyle(
			color = Color.WHITE,
			logoType = LogoType.INLINE,
			textAlignment = Align.RIGHT,
		)
		
		assertEquals(style1, style2)
		assertEquals(style1.hashCode(), style2.hashCode())
	}

	@Test
	fun testDifferentCombinations() {
		val style1 = PayPalMessageStyle(color = Color.GRAYSCALE)
		assertEquals(Color.GRAYSCALE, style1.color)
		assertEquals(LogoType.PRIMARY, style1.logoType)
		
		val style2 = PayPalMessageStyle(logoType = LogoType.NONE)
		assertEquals(Color.BLACK, style2.color)
		assertEquals(LogoType.NONE, style2.logoType)
		
		val style3 = PayPalMessageStyle(textAlignment = Align.CENTER)
		assertEquals(Align.CENTER, style3.textAlignment)
	}
}
