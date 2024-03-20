package com.paypal.messages.config.message

import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PayPalMessageViewStateCallbacksTest {
	@Test
	fun testConstructor() {
		val onLoadingMock = mockk<() -> Unit>("onLoadingMock")
		every { onLoadingMock() } answers {}
		val onSuccessMock = mockk<() -> Unit>("onSuccessMock")
		every { onSuccessMock() } answers {}
		val onErrorMock = mockk<(error: PayPalErrors.Base) -> Unit>("onErrorMock")
		every { onErrorMock(any()) } answers {}

		val messageViewState = PayPalMessageViewStateCallbacks(
			onLoading = onLoadingMock,
			onSuccess = onSuccessMock,
			onError = onErrorMock,
		)

		messageViewState.onLoading()
		messageViewState.onSuccess()
		messageViewState.onError(PayPalErrors.Base("", null))

		verify { onLoadingMock() }
		verify { onSuccessMock() }
		verify { onErrorMock(any()) }
	}

	@Test
	fun testClone() {
		val messageViewState = PayPalMessageViewStateCallbacks()
		val clonedMessageViewState = messageViewState.clone()

		assertEquals(messageViewState, clonedMessageViewState)
		clonedMessageViewState.onLoading = { }
		assertNotEquals(messageViewState, clonedMessageViewState)
	}
}
