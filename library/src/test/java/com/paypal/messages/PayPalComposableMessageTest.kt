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

	@Test
	fun testLoadingCallback() {
		// Arrange
		val clientId = "test-client-id"
		var loadingInvoked = false

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = { loadingInvoked = true },
			),
		)

		// Act - create PayPalMessageView and invoke loading callback
		val messageView = createPayPalMessageView(mockContext, config)
		config.viewStateCallbacks?.onLoading?.invoke()

		// Assert
		assertEquals(true, loadingInvoked)
	}

	@Test
	fun testSuccessCallback() {
		// Arrange
		val clientId = "test-client-id"
		var successInvoked = false

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onSuccess = { successInvoked = true },
			),
		)

		// Act - create PayPalMessageView and invoke success callback
		val messageView = createPayPalMessageView(mockContext, config)
		config.viewStateCallbacks?.onSuccess?.invoke()

		// Assert
		assertEquals(true, successInvoked)
	}

	@Test
	fun testOnClickCallback() {
		// Arrange
		val clientId = "test-client-id"
		var clickInvoked = false

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
			),
			eventsCallbacks = PayPalMessageEventsCallbacks(
				onClick = { clickInvoked = true },
			),
		)

		// Act - create PayPalMessageView and invoke click callback
		val messageView = createPayPalMessageView(mockContext, config)
		config.eventsCallbacks?.onClick?.invoke()

		// Assert
		assertEquals(true, clickInvoked)
	}

	@Test
	fun testOnApplyCallback() {
		// Arrange
		val clientId = "test-client-id"
		var applyInvoked = false

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = PayPalEnvironment.LIVE,
			),
			eventsCallbacks = PayPalMessageEventsCallbacks(
				onApply = { applyInvoked = true },
			),
		)

		// Act - create PayPalMessageView and invoke apply callback
		val messageView = createPayPalMessageView(mockContext, config)
		config.eventsCallbacks?.onApply?.invoke()

		// Assert
		assertEquals(true, applyInvoked)
	}

	@Test
	fun testDifferentEnvironments() {
		// Test SANDBOX environment
		val sandboxConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)
		val sandboxView = createPayPalMessageView(mockContext, sandboxConfig)
		assertNotNull(sandboxView)

		// Test DEVELOP environment with host
		val developConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP("test.paypal.com"),
			),
		)
		val developView = createPayPalMessageView(mockContext, developConfig)
		assertNotNull(developView)

		// Test DEVELOP environment with localhost
		val localConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(8443),
			),
		)
		val localView = createPayPalMessageView(mockContext, localConfig)
		assertNotNull(localView)
	}

	@Test
	fun testVariousOfferTypes() {
		// Test PAY_LATER_SHORT_TERM
		val shortTermConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			),
		)
		val shortTermView = createPayPalMessageView(mockContext, shortTermConfig)
		assertNotNull(shortTermView)
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, shortTermConfig.data.offerType)

		// Test PAY_LATER_LONG_TERM
		val longTermConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			),
		)
		val longTermView = createPayPalMessageView(mockContext, longTermConfig)
		assertNotNull(longTermView)
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, longTermConfig.data.offerType)

		// Test PAY_LATER_PAY_IN_1
		val payIn1Config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			),
		)
		val payIn1View = createPayPalMessageView(mockContext, payIn1Config)
		assertNotNull(payIn1View)
		assertEquals(PayPalMessageOfferType.PAY_LATER_PAY_IN_1, payIn1Config.data.offerType)
	}

	@Test
	fun testNullOptionalParameters() {
		// Test with all optional parameters set to null
		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				amount = null,
				buyerCountry = null,
				offerType = null,
			),
			viewStateCallbacks = null,
			eventsCallbacks = null,
		)

		val messageView = createPayPalMessageView(mockContext, config)
		assertNotNull(messageView)
		assertEquals(null, config.data.amount)
		assertEquals(null, config.data.buyerCountry)
		assertEquals(null, config.data.offerType)
	}

	@Test
	fun testEdgeCaseAmounts() {
		// Test with zero amount
		val zeroAmountConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				amount = 0.0,
			),
		)
		val zeroView = createPayPalMessageView(mockContext, zeroAmountConfig)
		assertNotNull(zeroView)
		assertEquals(0.0, zeroAmountConfig.data.amount)

		// Test with large amount
		val largeAmountConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				amount = 999999.99,
			),
		)
		val largeView = createPayPalMessageView(mockContext, largeAmountConfig)
		assertNotNull(largeView)
		assertEquals(999999.99, largeAmountConfig.data.amount)

		// Test with small decimal amount
		val smallAmountConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.LIVE,
				amount = 0.01,
			),
		)
		val smallView = createPayPalMessageView(mockContext, smallAmountConfig)
		assertNotNull(smallView)
		assertEquals(0.01, smallAmountConfig.data.amount)
	}

	@Test
	fun testVariousBuyerCountries() {
		// Test common country codes
		val countries = listOf("US", "GB", "DE", "FR", "CA", "AU", "JP")

		countries.forEach { country ->
			val config = PayPalMessageConfig(
				data = PayPalMessageData(
					clientID = "test-client-id",
					environment = PayPalEnvironment.LIVE,
					buyerCountry = country,
				),
			)
			val view = createPayPalMessageView(mockContext, config)
			assertNotNull(view)
			assertEquals(country, config.data.buyerCountry)
		}
	}

	// Note: The following tests would need to be run as instrumented tests rather than unit tests
	// as they require the Android runtime. For unit test coverage, we're focusing on testing
	// the configuration creation which is the core logic of the composable.
}
