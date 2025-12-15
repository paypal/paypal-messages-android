package com.paypal.messages.config.modal

import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PayPalMessagesModalConfigTest {
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

		val config = PayPalMessagesModalConfig(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_id",
			amount = 100.00,
			buyerCountry = "test_buyer_country",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			environment = PayPalEnvironment.SANDBOX,
			callbacks = modalEvents,
		)

		assertEquals(config.clientID, "test_client_id")
		assertEquals(config.merchantID, "test_merchant_id")
		assertEquals(config.partnerAttributionID, "test_partner_id")
		assertEquals(config.amount, 100.00)
		assertEquals(config.environment, PayPalEnvironment.SANDBOX)
		assertEquals(config.callbacks, modalEvents)
	}

	@Test
	fun testClone() {
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

		val oldData = PayPalMessagesModalConfig(
			clientID = "test_client_id",
			merchantID = "test_merchant_id",
			partnerAttributionID = "test_partner_id",
			amount = 100.00,
			buyerCountry = "test_buyer_country",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			environment = PayPalEnvironment.SANDBOX,
			callbacks = modalEvents,
		)

		val data = oldData.clone()

		assertEquals(data, oldData)
		data.amount = 200.0
		assertNotEquals(oldData, data)
	}
}
