package com.paypal.messages.config.modal

import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ModalEventsTest {
	@Test
	fun testConstructor() {
		val onClickMock = mockk<() -> Unit>("onClickMock")
		every { onClickMock() } answers {}
		val onApplyMock = mockk<() -> Unit>("onApplyMock")
		every { onApplyMock() } answers {}
		val onLoadingMock = mockk<() -> Unit>("onLoadingMock")
		every { onLoadingMock() } answers {}
		val onSuccessMock = mockk<() -> Unit>("onSuccessMock")
		every { onSuccessMock() } answers {}
		val onErrorMock = mockk<(error: PayPalErrors.Base) -> Unit>("onErrorMock")
		every { onErrorMock(any()) } answers {}
		val onCalculateMock = mockk<() -> Unit>("onCalculateMock")
		every { onCalculateMock() } answers {}
		val onShowMock = mockk<() -> Unit>("onShowMock")
		every { onShowMock() } answers {}
		val onCloseMock = mockk<() -> Unit>("onCloseMock")
		every { onCloseMock() } answers {}

		val modalEvents = ModalEvents(
			onClick = onClickMock,
			onApply = onApplyMock,
			onLoading = onLoadingMock,
			onSuccess = onSuccessMock,
			onError = onErrorMock,
			onCalculate = onCalculateMock,
			onShow = onShowMock,
			onClose = onCloseMock,
		)

		modalEvents.onClick()
		modalEvents.onApply()
		modalEvents.onLoading()
		modalEvents.onSuccess()
		modalEvents.onError(PayPalErrors.Base("", null))
		modalEvents.onCalculate()
		modalEvents.onShow()
		modalEvents.onClose()

		verify { onClickMock() }
		verify { onApplyMock() }
		verify { onLoadingMock() }
		verify { onSuccessMock() }
		verify { onErrorMock(any()) }
		verify { onCalculateMock() }
		verify { onShowMock() }
		verify { onCloseMock() }
	}
}
