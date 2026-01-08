package com.paypal.messages.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * Unit tests for PayPalMessageDataProvider.
 *
 * Uses Robolectric because PayPalMessageDataProvider uses Handler(Looper.getMainLooper()).
 *
 * These tests verify:
 * 1. fetchMessageData calls onLoading immediately
 * 2. fetchMessageData calls onSuccess on API success
 * 3. fetchMessageData calls onError on API failure
 * 4. fetchMessageData calls onError with default error when error is null
 * 5. createClickHandler returns a valid handler
 * 6. onCleanup properly cleans up resources
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PayPalMessageDataProviderTest {

	// Real instance - not mocked!
	private lateinit var dataProvider: PayPalMessageDataProvider
	private lateinit var mockConfig: PayPalMessageConfig
	private lateinit var instanceId: UUID

	@Before
	fun setup() {
		// Use REAL PayPalMessageDataProvider to test actual implementation
		dataProvider = PayPalMessageDataProvider()

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

	@After
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun `fetchMessageData calls onLoading immediately`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert - onLoading should be called synchronously before API call
		assertTrue("onLoading should be called immediately", loadingCalled)
	}

	@Test
	fun `fetchMessageData calls onSuccess on API success`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertTrue("onSuccess should be called", successCalled)
		assertEquals("Response should match", mockResponse, capturedResponse)
		assertNotNull("Duration should be captured", capturedDuration)
	}

	@Test
	fun `fetchMessageData calls onError on API failure`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertTrue("onError should be called", errorCalled)
		assertEquals("Error should match", expectedError, capturedError)
	}

	@Test
	fun `fetchMessageData calls onError with default error when error is null`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertTrue("onError should be called even with null error", errorCalled)
		assertNotNull("A default error should be provided", capturedError)
		assertTrue(
			"Default error message should indicate unknown error",
			capturedError?.message?.contains("Unknown error") == true,
		)
	}

	@Test
	fun `fetchMessageData handles 404 error`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertTrue(
			"Error should contain status code",
			capturedError?.message?.contains("404") == true,
		)
	}

	@Test
	fun `fetchMessageData handles 500 error`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertTrue(
			"Error should contain status code",
			capturedError?.message?.contains("500") == true,
		)
	}

	@Test
	fun `fetchMessageData handles InvalidResponseException`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
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
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertTrue(
			"Error should be InvalidResponseException",
			capturedError is PayPalErrors.InvalidResponseException,
		)
	}

	@Test
	fun `createClickHandler returns non-null handler`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()

		// Act
		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		// Assert
		assertNotNull("Click handler should not be null", clickHandler)
	}

	@Test
	fun `createClickHandler with log callback returns non-null handler`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()

		// Act
		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
		) { _ -> }

		// Assert
		assertNotNull("Click handler should not be null", clickHandler)
	}

	@Test
	fun `onCleanup can be called without error`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		// Act & Assert - should not throw
		clickHandler.onCleanup()
		assertTrue("onCleanup should complete without error", true)
	}

	@Test
	fun `multiple fetchMessageData calls work correctly`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var successCount = 0

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCount++
			}
			override fun onError(error: PayPalErrors.Base) {}
		}

		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val callbackSlot = slot<OnActionCompleted>()

		// First call succeeds
		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// Act
		dataProvider.fetchMessageData(context, mockConfig, UUID.randomUUID(), callback)
		dataProvider.fetchMessageData(context, mockConfig, UUID.randomUUID(), callback)

		// Assert
		assertEquals("Both calls should succeed", 2, successCount)
	}

	@Test
	fun `Api getMessageWithHash is called with correct parameters`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {}
			override fun onError(error: PayPalErrors.Base) {}
		}

		every { Api.getMessageWithHash(any(), any(), any(), any()) } answers {}

		// Act
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		verify {
			Api.getMessageWithHash(
				context,
				mockConfig,
				instanceId,
				any(),
			)
		}
	}

	// Direct tests for handleApiResult method to improve coverage

	@Test
	fun `handleApiResult calls onSuccess for Success result`() {
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
		val startTime = System.currentTimeMillis() - 100 // 100ms ago

		// Act
		dataProvider.handleApiResult(ApiResult.Success(mockResponse), startTime, callback)

		// Assert
		assertTrue("onSuccess should be called", successCalled)
		assertEquals("Response should match", mockResponse, capturedResponse)
		assertNotNull("Duration should be captured", capturedDuration)
		assertTrue("Duration should be positive", capturedDuration!! > 0)
	}

	@Test
	fun `handleApiResult calls onError for Failure result with error`() {
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

		val expectedError = PayPalErrors.FailedToFetchDataException("Test error", "debug-789")
		val startTime = System.currentTimeMillis()

		// Act
		dataProvider.handleApiResult(ApiResult.Failure(expectedError), startTime, callback)

		// Assert
		assertTrue("onError should be called", errorCalled)
		assertEquals("Error should match", expectedError, capturedError)
	}

	@Test
	fun `handleApiResult creates default error for Failure result with null error`() {
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

		val startTime = System.currentTimeMillis()

		// Act - pass null error to test the safety net
		dataProvider.handleApiResult(ApiResult.Failure(null), startTime, callback)

		// Assert
		assertTrue("onError should be called", errorCalled)
		assertNotNull("A default error should be created", capturedError)
		assertTrue(
			"Error should be FailedToFetchDataException",
			capturedError is PayPalErrors.FailedToFetchDataException,
		)
		assertTrue(
			"Error message should indicate unknown error",
			capturedError?.message?.contains("Unknown error occurred") == true,
		)
	}

	@Test
	fun `handleApiResult calculates duration correctly`() {
		// Arrange
		var capturedDuration: Int? = null

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				capturedDuration = requestDuration
			}
			override fun onError(error: PayPalErrors.Base) {}
		}

		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val startTime = System.currentTimeMillis() - 250 // 250ms ago

		// Act
		dataProvider.handleApiResult(ApiResult.Success(mockResponse), startTime, callback)

		// Assert
		assertNotNull("Duration should be captured", capturedDuration)
		// Duration should be approximately 250ms (allowing for some variance)
		assertTrue(
			"Duration should be around 250ms, was: $capturedDuration",
			capturedDuration!! >= 200 && capturedDuration!! <= 350,
		)
	}
}
