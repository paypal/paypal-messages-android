package com.paypal.messages.config.message

import com.paypal.messages.errors.BaseException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class PayPalMessageViewStateTest {
	@Test
	fun testConstructor() {
		val onLoadingMock = mockk<() -> Unit>("onLoadingMock")
		every { onLoadingMock() } answers {}
		val onSuccessMock = mockk<() -> Unit>("onSuccessMock")
		every { onSuccessMock() } answers {}
		val onErrorMock = mockk<(error: BaseException) -> Unit>("onErrorMock")
		every { onErrorMock(any()) } answers {}

		val messageViewState = PayPalMessageViewState(
			onLoading = onLoadingMock,
			onSuccess = onSuccessMock,
			onError = onErrorMock,
		)

		messageViewState.onLoading()
		messageViewState.onSuccess()
		messageViewState.onError(BaseException("", null))

		verify { onLoadingMock() }
		verify { onSuccessMock() }
		verify { onErrorMock(any()) }
	}
}

