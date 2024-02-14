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

	private val oldStyle = PayPalMessageStyle(
		color = Color.GRAYSCALE,
		logoType = LogoType.INLINE,
		textAlign = Align.RIGHT,
	)

	@Test
	fun testMergeWithNoParams() {
		val expectedStyle = oldStyle.copy()
		val actualStyle = oldStyle.merge(PayPalMessageStyle())

		assertEquals(expectedStyle, actualStyle)
	}

	@Test
	fun testMergeWithOneParam() {
		val actualColorStyle = oldStyle.merge(PayPalMessageStyle(color = Color.WHITE))
		val expectedColorStyle = PayPalMessageStyle(
			color = Color.WHITE,
			logoType = LogoType.INLINE,
			textAlign = Align.RIGHT,
		)
		assertEquals(expectedColorStyle, actualColorStyle)

		val actualLogoTypeStyle = oldStyle.merge(PayPalMessageStyle(logoType = LogoType.ALTERNATIVE))
		val expectedLogoTypeStyle = PayPalMessageStyle(
			color = Color.GRAYSCALE,
			logoType = LogoType.ALTERNATIVE,
			textAlign = Align.RIGHT,
		)
		assertEquals(expectedLogoTypeStyle, actualLogoTypeStyle)

		val actualTextAlignStyle = oldStyle.merge(PayPalMessageStyle(textAlign = Align.CENTER))
		val expectedTextAlignStyle = PayPalMessageStyle(
			color = Color.GRAYSCALE,
			logoType = LogoType.INLINE,
			textAlign = Align.CENTER,
		)
		assertEquals(expectedTextAlignStyle, actualTextAlignStyle)
	}

	@Test
	fun testMergeWithThreeParams() {
		val expectedStyle = PayPalMessageStyle(
			color = Color.WHITE,
			logoType = LogoType.ALTERNATIVE,
			textAlign = Align.RIGHT,
		)
		val actualStyle = oldStyle.merge(expectedStyle)
		assertEquals(expectedStyle, actualStyle)
	}

	@Test
	fun testClone() {
		val messageStyle = PayPalMessageStyle()
		val clonedMessageStyle = messageStyle.clone()

		assertEquals(messageStyle, clonedMessageStyle)
	}
}
