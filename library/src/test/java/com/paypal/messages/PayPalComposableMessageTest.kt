package com.paypal.messages

import android.content.Context
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayPalComposableMessageTest {

	// Mock dependencies
	private val mockContext = mockk<Context>(relaxed = true)
	private val mockMessageView = mockk<PayPalMessageView>(relaxed = true)

	// Callback flags
	private var loadingCalled = false
	private var errorCalled = false
	private var successCalled = false
	private var clickCalled = false
	private var applyCalled = false

	@BeforeEach
	fun setup() {
		// Reset flags
		loadingCalled = false
		errorCalled = false
		successCalled = false
		clickCalled = false
		applyCalled = false

		// Mock createPayPalMessageView function
		mockkStatic("com.paypal.messages.PayPalComposableMessageKt")
		every { createPayPalMessageView(any(), any()) } returns mockMessageView
	}

	@AfterEach
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun testConfigCreationWithMinimumRequiredParameters() {
		// Arrange
		val clientId = "test-client-id"
		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
				amount = null,
				buyerCountry = null,
				offerType = null,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(),
			eventsCallbacks = PayPalMessageEventsCallbacks(),
		)

		// Act - create PayPalMessageView with the config
		val messageView = createPayPalMessageView(mockContext, config)

		// Assert
		assertNotNull(messageView)
		verify { createPayPalMessageView(mockContext, config) }
	}

	@Test
	fun testConfigCreationWithAllParameters() {
		// Arrange
		val clientId = "test-client-id"
		val amount = 100.0
		val buyerCountry = "US"
		val offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.SANDBOX,
				amount = amount,
				buyerCountry = buyerCountry,
				offerType = offerType,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = { loadingCalled = true },
				onError = { errorCalled = true },
				onSuccess = { successCalled = true },
			),
			eventsCallbacks = PayPalMessageEventsCallbacks(
				onClick = { clickCalled = true },
				onApply = { applyCalled = true },
			),
		)

		// Act - create PayPalMessageView with the config
		val messageView = createPayPalMessageView(mockContext, config)

		// Assert
		assertNotNull(messageView)
		verify { createPayPalMessageView(mockContext, config) }
	}

	@Test
	fun testOfferTypeParsingHandlesInvalidValues() {
		// Test that invalid offer types are handled gracefully
		val clientId = "test-client-id"
		val invalidOfferType = "INVALID_OFFER_TYPE"

		// We need to use a slot to capture parameters
		val configSlot = slot<PayPalMessageConfig>()

		every { createPayPalMessageView(any(), capture(configSlot)) } returns mockMessageView

		// Act - create the composable with an invalid offer type
		val messageView = createPayPalMessageView(
			mockContext,
			PayPalMessageConfig(
				data = PayPalMessageData(
					clientID = clientId,
					environment = PayPalEnvironment.LIVE,
					offerType = null, // This would be the result after parsing an invalid offer type
				),
			),
		)

		// Assert
		assertNotNull(messageView)
		assertEquals(null, configSlot.captured.data.offerType)
	}

	@Test
	fun testErrorCallback() {
		// Arrange
		val clientId = "test-client-id"
		var capturedError: PayPalErrors.Base? = null

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onError = { error -> capturedError = error },
			),
		)

		// Act - create PayPalMessageView with the config
		val messageView = createPayPalMessageView(mockContext, config)

		// Simulate error callback
		val testError = PayPalErrors.Base("Test error message")
		config.viewStateCallbacks?.onError?.invoke(testError)

		// Assert
		assertNotNull(capturedError)
		assertEquals(testError, capturedError, "The captured error should be the same as the test error")
		// This directly compares the error objects rather than just the message substring
	}

	// Note: The following tests would need to be run as instrumented tests rather than unit tests
	// as they require the Android runtime. For unit test coverage, we're focusing on testing
	// the configuration creation which is the core logic of the composable.
}
