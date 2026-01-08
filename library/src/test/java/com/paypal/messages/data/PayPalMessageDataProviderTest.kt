package com.paypal.messages.data

import android.content.Context
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
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
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Unit tests for PayPalMessageDataProvider.
 *
 * These tests verify:
 * 1. fetchMessageData calls onLoading immediately
 * 2. fetchMessageData calls onSuccess on API success
 * 3. fetchMessageData calls onError on API failure
 * 4. fetchMessageData calls onError with default error when error is null
 * 5. createClickHandler returns a valid handler
 * 6. onCleanup properly cleans up resources
 */
class PayPalMessageDataProviderTest {

	// Real instance - not mocked!
	private lateinit var dataProvider: PayPalMessageDataProvider
	private lateinit var mockContext: Context
	private lateinit var mockConfig: PayPalMessageConfig
	private lateinit var instanceId: UUID

	@BeforeEach
	fun setup() {
		// Use REAL PayPalMessageDataProvider to test actual implementation
		dataProvider = PayPalMessageDataProvider()

		// Mock external dependencies
		mockContext = mockk(relaxed = true)
		instanceId = UUID.randomUUID()

		// Mock the Api singleton
		mockkObject(Api)

		// Create config
		mockConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
				amount = 100.0,
				buyerCountry = "US",
				offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			),
		)
	}

	@AfterEach
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun `fetchMessageData calls onLoading immediately`() {
		// Arrange
		var loadingCalled = false
		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {
				loadingCalled = true
			}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {}
		}

		// Mock API to capture callback but not invoke it yet
		every { Api.getMessageWithHash(any(), any(), any(), any()) } answers {}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert - onLoading should be called synchronously before API call
		assertTrue(loadingCalled, "onLoading should be called immediately")
	}

	@Test
	fun `fetchMessageData calls onSuccess on API success`() {
		// Arrange
		var successCalled = false
		var capturedResponse: ApiMessageData.Response? = null
		var capturedDuration: Int? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCalled = true
				capturedResponse = response
				capturedDuration = requestDuration
			}
			override fun onError(error: PayPalErrors.Base) {}
		}

		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			// Simulate async callback
			callbackSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertTrue(successCalled, "onSuccess should be called")
		assertEquals(mockResponse, capturedResponse, "Response should match")
		assertNotNull(capturedDuration, "Duration should be captured")
	}

	@Test
	fun `fetchMessageData calls onError on API failure`() {
		// Arrange
		var errorCalled = false
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {
				errorCalled = true
				capturedError = error
			}
		}

		val expectedError = PayPalErrors.FailedToFetchDataException("Test error", "debug-123")
		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Failure(expectedError))
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertTrue(errorCalled, "onError should be called")
		assertEquals(expectedError, capturedError, "Error should match")
	}

	@Test
	fun `fetchMessageData calls onError with default error when error is null`() {
		// Arrange
		var errorCalled = false
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {
				errorCalled = true
				capturedError = error
			}
		}

		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			// Simulate failure with null error
			callbackSlot.captured.onActionCompleted(ApiResult.Failure(null))
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertTrue(errorCalled, "onError should be called even with null error")
		assertNotNull(capturedError, "A default error should be provided")
		assertTrue(
			capturedError?.message?.contains("Unknown error") == true,
			"Default error message should indicate unknown error",
		)
	}

	@Test
	fun `fetchMessageData handles 404 error`() {
		// Arrange
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {
				capturedError = error
			}
		}

		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Code was 404")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("404") == true,
			"Error should contain status code",
		)
	}

	@Test
	fun `fetchMessageData handles 500 error`() {
		// Arrange
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {
				capturedError = error
			}
		}

		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Code was 500")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("500") == true,
			"Error should contain status code",
		)
	}

	@Test
	fun `fetchMessageData handles InvalidResponseException`() {
		// Arrange
		var capturedError: PayPalErrors.Base? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {
				capturedError = error
			}
		}

		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(
				ApiResult.Failure(PayPalErrors.InvalidResponseException("Invalid response data", "debug-456")),
			)
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError is PayPalErrors.InvalidResponseException,
			"Error should be InvalidResponseException",
		)
	}

	@Test
	fun `createClickHandler returns non-null handler`() {
		// Act
		val clickHandler = dataProvider.createClickHandler(
			mockContext,
			mockConfig,
			instanceId,
			null,
		)

		// Assert
		assertNotNull(clickHandler, "Click handler should not be null")
	}

	@Test
	fun `createClickHandler with log callback returns non-null handler`() {
		// Arrange
		var logCallbackInvoked = false

		// Act
		val clickHandler = dataProvider.createClickHandler(
			mockContext,
			mockConfig,
			instanceId,
		) { event ->
			logCallbackInvoked = true
		}

		// Assert
		assertNotNull(clickHandler, "Click handler should not be null")
	}

	@Test
	fun `onCleanup can be called without error`() {
		// Arrange
		val clickHandler = dataProvider.createClickHandler(
			mockContext,
			mockConfig,
			instanceId,
			null,
		)

		// Act & Assert - should not throw
		clickHandler.onCleanup()
		assertTrue(true, "onCleanup should complete without error")
	}

	@Test
	fun `multiple fetchMessageData calls work correctly`() {
		// Arrange
		var successCount = 0
		var errorCount = 0

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCount++
			}
			override fun onError(error: PayPalErrors.Base) {
				errorCount++
			}
		}

		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val callbackSlot = slot<OnActionCompleted>()

		// First call succeeds
		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, UUID.randomUUID(), callback)
		dataProvider.fetchMessageData(mockContext, mockConfig, UUID.randomUUID(), callback)

		// Assert
		assertEquals(2, successCount, "Both calls should succeed")
		assertEquals(0, errorCount, "No errors should occur")
	}

	@Test
	fun `Api getMessageWithHash is called with correct parameters`() {
		// Arrange
		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {}
		}

		every { Api.getMessageWithHash(any(), any(), any(), any()) } answers {}

		// Act
		dataProvider.fetchMessageData(mockContext, mockConfig, instanceId, callback)

		// Assert
		verify {
			Api.getMessageWithHash(
				mockContext,
				mockConfig,
				instanceId,
				any(),
			)
		}
	}
}
