package com.paypal.messages

import android.content.Context
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.data.PayPalMessageDataCallback
import com.paypal.messages.data.PayPalMessageDataProvider
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Unit tests for error handling in PayPal Messaging SDK.
 *
 * These tests verify that:
 * 1. onPayPalMessagingFailure is invoked exactly once per request when errors occur
 * 2. onPayPalMessagingLoading transitions to a terminal state (success or failure)
 * 3. No path leaves the client stuck in a loading state
 * 4. All error types are properly handled (network, 4xx/5xx, parsing, timeout)
 */
class PayPalMessageViewErrorTest {

	private lateinit var mockContext: Context
	private lateinit var dataProvider: PayPalMessageDataProvider
	private val instanceId = UUID.randomUUID()

	@BeforeEach
	fun setup() {
		mockContext = mockk(relaxed = true)
		dataProvider = PayPalMessageDataProvider()
		mockkObject(Api)
	}

	@AfterEach
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun `test network failure invokes onError exactly once`() {
		// Arrange
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				// Should not be called
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate network failure
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Network error")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("Network error") ?: false,
			"Error message should contain 'Network error'",
		)
	}

	@Test
	fun `test 4xx error invokes onError exactly once`() {
		// Arrange
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				// Should not be called
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate 404 error
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Code was 404")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("Code was 404") ?: false,
			"Error message should contain '404'",
		)
	}

	@Test
	fun `test 5xx error invokes onError exactly once`() {
		// Arrange
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				// Should not be called
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate 502 error
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Code was 502")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("Code was 502") ?: false,
			"Error message should contain '502'",
		)
	}

	@Test
	fun `test parsing error invokes onError exactly once`() {
		// Arrange
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				// Should not be called
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate parsing error
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(
					PayPalErrors.FailedToFetchDataException("Failed to parse response: Invalid JSON"),
				),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("parse") ?: false,
			"Error message should contain 'parse'",
		)
	}

	@Test
	fun `test eligibility false invokes onError exactly once`() {
		// Arrange
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				// Should not be called
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate eligibility failure
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.InvalidResponseException("Not eligible")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
	}

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
	fun `test loading always transitions to terminal state on success`() {
		// Arrange
		var loadingCallCount = 0
		var successCallCount = 0
		var errorCallCount = 0

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCallCount++
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate success
		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(1, successCallCount, "onSuccess should be called exactly once")
		assertEquals(0, errorCallCount, "onError should not be called on success")
	}

	@Test
	fun `test loading always transitions to terminal state on error`() {
		// Arrange
		var loadingCallCount = 0
		var successCallCount = 0
		var errorCallCount = 0

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCallCount++
			}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCallCount++
			}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to simulate error
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Error")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertEquals(0, successCallCount, "onSuccess should not be called on error")
		assertEquals(1, errorCallCount, "onError should be called exactly once")
	}

	@Test
	fun `test ApiResult Failure with null error still invokes onError`() {
		// Arrange
		var errorCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}

			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}

			override fun onError(error: PayPalErrors.Base) {
				errorCallCount++
				capturedError = error
			}
		}

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API to return Failure without an error (edge case)
		// This tests the safety net in PayPalMessageDataProvider that ensures onError is always called
		val callbackSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot))
		} answers {
			// Simulate a failure result with no error details
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(null),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, config, instanceId, callback)

		// Assert
		assertEquals(1, errorCallCount, "onError should be called even when error details are missing")
		assertNotNull(capturedError, "A default error should be created")
		assertTrue(
			capturedError?.message?.contains("Unknown error") ?: false,
			"Default error message should be provided when error details are missing",
		)
	}
}
