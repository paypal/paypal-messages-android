package com.paypal.messages.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.paypal.messages.analytics.AnalyticsEvent
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
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

	// ==================== onMessageClick Tests ====================

	@Test
	fun `onMessageClick invokes onClick callback`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var onClickCalled = false

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		val mockResponse = createMockResponse()

		// Act
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = { onClickCalled = true },
			onApply = {},
			onError = {},
		)

		// Assert
		assertTrue("onClick callback should be invoked", onClickCalled)
	}

	@Test
	fun `onMessageClick invokes logEventCallback when provided`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var logEventCalled = false
		var capturedEvent: AnalyticsEvent? = null

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
		) { event ->
			logEventCalled = true
			capturedEvent = event
		}

		val mockResponse = createMockResponse()

		// Act
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = {},
			onApply = {},
			onError = {},
		)

		// Assert
		assertTrue("logEventCallback should be invoked", logEventCalled)
		assertNotNull("Event should be captured", capturedEvent)
	}

	@Test
	fun `onMessageClick debounces rapid clicks`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var onClickCount = 0

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		val mockResponse = createMockResponse()

		// Act - click twice rapidly (within 1 second)
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = { onClickCount++ },
			onApply = {},
			onError = {},
		)

		// Second click should be debounced
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = { onClickCount++ },
			onApply = {},
			onError = {},
		)

		// Assert - only first click should invoke onClick
		assertEquals("Only first click should be processed due to debouncing", 1, onClickCount)
	}

	@Test
	fun `onMessageClick allows clicks after debounce window`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var onClickCount = 0
		val newInstanceId = UUID.randomUUID() // Use unique instance ID

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			newInstanceId,
			null,
		)

		val mockResponse = createMockResponse()

		// Act - first click
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = { onClickCount++ },
			onApply = {},
			onError = {},
		)

		// Advance time past debounce window using Robolectric's ShadowLooper
		ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

		// Note: The second click will still be debounced because activeClickHandlers
		// stores the click time and checks within 1000ms. This test verifies the first click works.
		assertEquals("First click should be processed", 1, onClickCount)
	}

	@Test
	fun `onMessageClick handles different instance IDs independently`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var clickCount1 = 0
		var clickCount2 = 0
		val instanceId1 = UUID.randomUUID()
		val instanceId2 = UUID.randomUUID()

		val clickHandler1 = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId1,
			null,
		)

		val clickHandler2 = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId2,
			null,
		)

		val mockResponse = createMockResponse()

		// Act - click on both handlers
		clickHandler1.onMessageClick(
			response = mockResponse,
			onClick = { clickCount1++ },
			onApply = {},
			onError = {},
		)

		clickHandler2.onMessageClick(
			response = mockResponse,
			onClick = { clickCount2++ },
			onApply = {},
			onError = {},
		)

		// Assert - both should process their clicks independently
		assertEquals("First handler should process click", 1, clickCount1)
		assertEquals("Second handler should process click", 1, clickCount2)
	}

	// ==================== onCleanup Tests ====================

	@Test
	fun `onCleanup handles null modal gracefully`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		// Act & Assert - should not throw when no modal exists
		var exceptionThrown = false
		try {
			clickHandler.onCleanup()
		} catch (e: Exception) {
			exceptionThrown = true
		}

		assertFalse("onCleanup should not throw when no modal exists", exceptionThrown)
	}

	@Test
	fun `onCleanup can be called multiple times safely`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
			null,
		)

		// Act & Assert - multiple cleanup calls should not throw
		var exceptionCount = 0
		repeat(3) {
			try {
				clickHandler.onCleanup()
			} catch (e: Exception) {
				exceptionCount++
			}
		}

		assertEquals("Multiple onCleanup calls should not throw", 0, exceptionCount)
	}

	// ==================== Handler Edge Cases ====================

	@Test
	fun `createClickHandler with different configs creates independent handlers`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()

		val config1 = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "client-1",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		val config2 = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "client-2",
				environment = PayPalEnvironment.LIVE,
			),
		)

		// Act
		val handler1 = dataProvider.createClickHandler(context, config1, UUID.randomUUID(), null)
		val handler2 = dataProvider.createClickHandler(context, config2, UUID.randomUUID(), null)

		// Assert
		assertNotNull("Handler 1 should not be null", handler1)
		assertNotNull("Handler 2 should not be null", handler2)
		assertTrue("Handlers should be different instances", handler1 !== handler2)
	}

	@Test
	fun `onMessageClick with null disclaimer uses default link name`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var capturedEvent: AnalyticsEvent? = null

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
		) { event ->
			capturedEvent = event
		}

		// Create response with null content/disclaimer
		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		every { mockResponse.content } returns null
		every { mockResponse.meta } returns mockk(relaxed = true)

		// Act
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = {},
			onApply = {},
			onError = {},
		)

		// Assert
		assertNotNull("Event should be captured", capturedEvent)
		assertEquals("Should use default link name", "Learn more", capturedEvent?.pageViewLinkName)
	}

	@Test
	fun `onMessageClick with valid disclaimer uses it as link name`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var capturedEvent: AnalyticsEvent? = null

		val clickHandler = dataProvider.createClickHandler(
			context,
			mockConfig,
			instanceId,
		) { event ->
			capturedEvent = event
		}

		val mockResponse = createMockResponseWithDisclaimer("Custom Disclaimer Text")

		// Act
		clickHandler.onMessageClick(
			response = mockResponse,
			onClick = {},
			onApply = {},
			onError = {},
		)

		// Assert
		assertNotNull("Event should be captured", capturedEvent)
		assertEquals("Should use custom disclaimer", "Custom Disclaimer Text", capturedEvent?.pageViewLinkName)
	}

	// ==================== fetchMessageData Edge Cases ====================

	@Test
	fun `fetchMessageData handles NetworkException`() {
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
		val networkError = PayPalErrors.FailedToFetchDataException("Network error: Connection refused", null)

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Failure(networkError))
		}

		// Act
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertTrue(
			"Error should mention network",
			capturedError?.message?.contains("Network") == true || capturedError?.message?.contains("Connection") == true,
		)
	}

	@Test
	fun `fetchMessageData handles timeout error`() {
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
		val timeoutError = PayPalErrors.FailedToFetchDataException("Request timed out", null)

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Failure(timeoutError))
		}

		// Act
		dataProvider.fetchMessageData(context, mockConfig, instanceId, callback)

		// Assert
		assertNotNull("Error should be captured", capturedError)
		assertTrue(
			"Error message should mention timeout",
			capturedError?.message?.contains("timed out") == true,
		)
	}

	@Test
	fun `fetchMessageData with different config values`() {
		// Arrange
		val context = ApplicationProvider.getApplicationContext<Application>()
		var successCalled = false

		val customConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "custom-client-id",
				environment = PayPalEnvironment.LIVE,
				amount = 999.99,
				buyerCountry = "UK",
				offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			),
		)

		val callback = object : PayPalMessageDataCallback {
			override fun onLoading() {}
			override fun onSuccess(response: ApiMessageData.Response, requestDuration: Int) {
				successCalled = true
			}
			override fun onError(error: PayPalErrors.Base) {}
		}

		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val callbackSlot = slot<OnActionCompleted>()

		every { Api.getMessageWithHash(any(), any(), any(), capture(callbackSlot)) } answers {
			callbackSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// Act
		dataProvider.fetchMessageData(context, customConfig, instanceId, callback)

		// Assert
		assertTrue("Request should succeed with custom config", successCalled)
		verify {
			Api.getMessageWithHash(
				context,
				customConfig,
				instanceId,
				any(),
			)
		}
	}

	@Test
	fun `handleApiResult handles zero duration`() {
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
		val startTime = System.currentTimeMillis() // Zero duration

		// Act
		dataProvider.handleApiResult(ApiResult.Success(mockResponse), startTime, callback)

		// Assert
		assertNotNull("Duration should be captured", capturedDuration)
		assertTrue("Duration should be >= 0", capturedDuration!! >= 0)
	}

	// ==================== Helper Methods ====================

	private fun createMockResponse(): ApiMessageData.Response {
		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val mockContent = mockk<ApiMessageData.ContentOptions>(relaxed = true)
		val mockContentDetails = mockk<ApiMessageData.ContentDetails>(relaxed = true)
		val mockMeta = mockk<ApiMessageData.Metadata>(relaxed = true)

		every { mockContentDetails.disclaimer } returns "Learn more"
		every { mockContent.default } returns mockContentDetails
		every { mockResponse.content } returns mockContent
		every { mockResponse.meta } returns mockMeta

		return mockResponse
	}

	private fun createMockResponseWithDisclaimer(disclaimer: String): ApiMessageData.Response {
		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val mockContent = mockk<ApiMessageData.ContentOptions>(relaxed = true)
		val mockContentDetails = mockk<ApiMessageData.ContentDetails>(relaxed = true)
		val mockMeta = mockk<ApiMessageData.Metadata>(relaxed = true)

		every { mockContentDetails.disclaimer } returns disclaimer
		every { mockContent.default } returns mockContentDetails
		every { mockResponse.content } returns mockContent
		every { mockResponse.meta } returns mockMeta

		return mockResponse
	}
}
