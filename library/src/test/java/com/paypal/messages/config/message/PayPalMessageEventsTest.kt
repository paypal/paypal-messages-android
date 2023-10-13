package com.paypal.messages.config.message

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PayPalMessageEventsTest {
	@Test
	fun testConstructor() {
		val onClickMock = mockk<() -> Unit>("onClickMock")
		every { onClickMock() } answers {}
		val onApplyMock = mockk<() -> Unit>("onApplyMock")
		every { onApplyMock() } answers {}

		val messageEvents = PayPalMessageEvents(
			onClick = onClickMock,
			onApply = onApplyMock,
		)

		messageEvents.onClick()
		messageEvents.onApply()

		verify { onClickMock() }
		verify { onApplyMock() }
	}
}
