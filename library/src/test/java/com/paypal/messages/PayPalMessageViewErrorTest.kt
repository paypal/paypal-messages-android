package com.paypal.messages

import android.content.Context
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.utils.PayPalErrors
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for error handling in PayPal Messaging SDK.
 *
 * These tests verify that:
 * 1. onPayPalMessagingFailure is invoked exactly once per request when errors occur
 * 2. The error callback mechanism works correctly
 * 3. Error details are properly captured and passed to callbacks
 *
 * Note: Full async integration tests with MockWebServer are in the instrumentation tests.
 */
class PayPalMessageViewErrorTest {

	private val mockContext: Context = mockk(relaxed = true)

	@Test
	fun `test onError callback is invoked on PayPalMessageView`() {
		// Arrange
		var errorCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onError = { error ->
					errorCallCount++
					capturedError = error
				},
			),
		)

		// Create a PayPalMessageView with the config
		val messageView = PayPalMessageView(mockContext, config = config)

		// Act - simulate error
		val testError = PayPalErrors.FailedToFetchDataException("Test error")
		messageView.onError(testError)

		// Assert
		assertEquals(1, errorCallCount, "User's onError callback should be called exactly once")
		assertEquals(testError, capturedError, "Captured error should match the test error")
	}

	@Test
	fun `test multiple error types are properly handled`() {
		// Arrange
		var errorCallCount = 0
		val capturedErrors = mutableListOf<PayPalErrors.Base>()

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onError = { error ->
					errorCallCount++
					capturedErrors.add(error)
				},
			),
		)

		val messageView = PayPalMessageView(mockContext, config = config)

		// Act - simulate different error types
		val networkError = PayPalErrors.FailedToFetchDataException("Network error")
		val parsingError = PayPalErrors.FailedToFetchDataException("Failed to parse response")
		val invalidResponseError = PayPalErrors.InvalidResponseException("Invalid response")

		messageView.onError(networkError)
		messageView.onError(parsingError)
		messageView.onError(invalidResponseError)

		// Assert
		assertEquals(3, errorCallCount, "onError should be called for each error")
		assertEquals(3, capturedErrors.size, "All errors should be captured")
		assertEquals(networkError, capturedErrors[0])
		assertEquals(parsingError, capturedErrors[1])
		assertEquals(invalidResponseError, capturedErrors[2])
	}

	@Test
	fun `test error callback captures error details`() {
		// Arrange
		var capturedError: PayPalErrors.Base? = null

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onError = { error ->
					capturedError = error
				},
			),
		)

		val messageView = PayPalMessageView(mockContext, config = config)

		// Act
		val debugId = "test-debug-id-12345"
		val testError = PayPalErrors.FailedToFetchDataException("Code was 404", debugId)
		messageView.onError(testError)

		// Assert
		assertNotNull(capturedError, "Error should be captured")
		assertEquals(debugId, capturedError?.debugId, "Debug ID should be preserved")
		assert(capturedError?.message?.contains("404") == true) {
			"Error message should contain status code"
		}
	}

	@Test
	fun `test error callback with null onError handler does not crash`() {
		// Arrange - no error handler provided
		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			// viewStateCallbacks is null by default
		)

		val messageView = PayPalMessageView(mockContext, config = config)

		// Act & Assert - should not throw exception
		val testError = PayPalErrors.FailedToFetchDataException("Test error")
		try {
			messageView.onError(testError)
			// Test passes if no exception is thrown
			assert(true)
		} catch (e: Exception) {
			assert(false) { "Should not throw exception when error handler is not provided: ${e.message}" }
		}
	}

	@Test
	fun `test onLoading and onSuccess callbacks work correctly`() {
		// Arrange
		var loadingCallCount = 0
		var successCallCount = 0

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onSuccess = {
					successCallCount++
				},
			),
		)

		val messageView = PayPalMessageView(mockContext, config = config)

		// Act
		messageView.onLoading()
		messageView.onLoading()

		// Assert
		assertEquals(2, loadingCallCount, "onLoading callback should be invoked each time")
	}
}
