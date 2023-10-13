package com.paypal.messages.config.modal

import com.paypal.messages.config.Channel
import com.paypal.messages.config.PayPalMessageOfferType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModalConfigTest {
	@Test
	fun testConstructor() {
		val modalConfig = ModalConfig(
			amount = 115.00,
			currency = "USD",
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			ignoreCache = true,
			devTouchpoint = true,
			stageTag = "test_tag",
			events = ModalEvents(),
			modalCloseButton = ModalCloseButton(
				width = 100,
				height = 100,
				availableWidth = 200,
				availableHeight = 200,
				color = "#FFFFFF",
				colorType = "solid",
			),
		)

		assertEquals(modalConfig.amount, 115.00)
		assertEquals(modalConfig.currency, "USD")
		assertEquals(modalConfig.buyerCountry, "US")
		assertEquals(modalConfig.offer, PayPalMessageOfferType.PAY_LATER_SHORT_TERM)
		assertEquals(modalConfig.channel, Channel.NATIVE)
		assertEquals(modalConfig.ignoreCache, true)
		assertEquals(modalConfig.devTouchpoint, true)
		assertEquals(modalConfig.stageTag, "test_tag")
		assertEquals(modalConfig.events, ModalEvents())
		assertEquals(
			modalConfig.modalCloseButton,
			ModalCloseButton(
				width = 100,
				height = 100,
				availableWidth = 200,
				availableHeight = 200,
				color = "#FFFFFF",
				colorType = "solid",
			),
		)
	}
}
