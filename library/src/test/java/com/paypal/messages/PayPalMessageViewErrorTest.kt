package com.paypal.messages

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.utils.PayPalErrors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for error handling in PayPal Messaging SDK.
 *
 * These tests verify that:
 * 1. onPayPalMessagingFailure is invoked exactly once per request when errors occur
 * 2. The error callback mechanism works correctly
 * 3. Error details are properly captured and passed to callbacks
 *
 * Uses Robolectric to provide Android runtime for PayPalMessageView instantiation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PayPalMessageViewErrorTest {

	private fun getContext() = ContextThemeWrapper(
		ApplicationProvider.getApplicationContext<Application>(),
		R.style.Theme_PayPalModal,
	)

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
		val messageView = PayPalMessageView(getContext(), config = config)

		// Act - simulate error
		val testError = PayPalErrors.FailedToFetchDataException("Test error")
		messageView.onError(testError)

		// Assert
		assertEquals("User's onError callback should be called exactly once", 1, errorCallCount)
		assertEquals("Captured error should match the test error", testError, capturedError)
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

		val messageView = PayPalMessageView(getContext(), config = config)

		// Act - simulate different error types
		val networkError = PayPalErrors.FailedToFetchDataException("Network error")
		val parsingError = PayPalErrors.FailedToFetchDataException("Failed to parse response")
		val invalidResponseError = PayPalErrors.InvalidResponseException("Invalid response")

		messageView.onError(networkError)
		messageView.onError(parsingError)
		messageView.onError(invalidResponseError)

		// Assert
		assertEquals("onError should be called for each error", 3, errorCallCount)
		assertEquals("All errors should be captured", 3, capturedErrors.size)
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

		val messageView = PayPalMessageView(getContext(), config = config)

		// Act
		val debugId = "test-debug-id-12345"
		val testError = PayPalErrors.FailedToFetchDataException("Code was 404", debugId)
		messageView.onError(testError)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertEquals("Debug ID should be preserved", debugId, capturedError?.debugId)
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

		val messageView = PayPalMessageView(getContext(), config = config)

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
	fun `test onLoading callback is invoked`() {
		// Arrange
		var loadingCallCount = 0

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
			),
		)

		val messageView = PayPalMessageView(getContext(), config = config)

		// Note: fetchMessageData calls onLoading() during init, so record the count after init
		val countAfterInit = loadingCallCount

		// Act - call onLoading twice more
		messageView.onLoading()
		messageView.onLoading()

		// Assert - verify callback was invoked 2 additional times
		assertEquals(
			"onLoading callback should be invoked each time it's called",
			countAfterInit + 2,
			loadingCallCount,
		)
	}
}
