package com.paypal.messages.config.message

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PayPalMessageEventsCallbacksTest {
	@Test
	fun testConstructor() {
		val onClickMock = mockk<() -> Unit>("onClickMock")
		every { onClickMock() } answers {}
		val onApplyMock = mockk<() -> Unit>("onApplyMock")
		every { onApplyMock() } answers {}

		val messageEvents = PayPalMessageEventsCallbacks(
			onClick = onClickMock,
			onApply = onApplyMock,
		)

		messageEvents.onClick()
		messageEvents.onApply()

		verify { onClickMock() }
		verify { onApplyMock() }
	}

	@Test
	fun testClone() {
		val messageEvents = PayPalMessageEventsCallbacks()
		val clonedMessageEvents = messageEvents.clone()

		assertEquals(messageEvents, clonedMessageEvents)
		clonedMessageEvents.onClick = { "NOT EQUAL" }
		assertNotEquals(messageEvents, clonedMessageEvents)
	}
}
