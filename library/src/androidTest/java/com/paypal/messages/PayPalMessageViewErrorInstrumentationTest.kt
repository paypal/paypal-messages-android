package com.paypal.messages

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.io.Api
import com.paypal.messages.utils.PayPalErrors
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumentation tests for error handling in PayPal Messaging SDK.
 *
 * These tests verify the error handling behavior with real Android runtime
 * and simulated backend failures using MockWebServer.
 *
 * Acceptance Criteria:
 * 1. When message data fetch fails for any reason, onPayPalMessagingFailure is invoked exactly once
 * 2. onPayPalMessagingLoading must transition to a terminal state (success or failure)
 * 3. No path should leave the client stuck in a loading state
 */
@RunWith(AndroidJUnit4::class)
class PayPalMessageViewErrorInstrumentationTest {

	private lateinit var context: Context
	private lateinit var mockWebServer: MockWebServer

	@Before
	fun setup() {
		context = ApplicationProvider.getApplicationContext()
		mockWebServer = MockWebServer()
		mockWebServer.start()
	}

	@After
	fun tearDown() {
		mockWebServer.shutdown()
	}

	@Test
	fun testNetworkErrorInvokesOnErrorExactlyOnce() {
		// Arrange
		val latch = CountDownLatch(1)
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Simulate network error by not enqueuing any response
		mockWebServer.shutdown() // Force connection failure

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onError = { error ->
					errorCallCount++
					capturedError = error
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for error callback
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Error callback should be invoked within timeout", completed)
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("error") ?: false,
			"Error message should indicate a failure",
		)
	}

	@Test
	fun test404ErrorInvokesOnErrorExactlyOnce() {
		// Arrange
		val latch = CountDownLatch(1)
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Enqueue 404 response
		mockWebServer.enqueue(
			MockResponse()
				.setResponseCode(404)
				.setBody("{\"error\": \"Not Found\"}"),
		)

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onError = { error ->
					errorCallCount++
					capturedError = error
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for error callback
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Error callback should be invoked within timeout", completed)
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("404") ?: false,
			"Error message should contain '404'",
		)
	}

	@Test
	fun test500ErrorInvokesOnErrorExactlyOnce() {
		// Arrange
		val latch = CountDownLatch(1)
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Enqueue 500 response
		mockWebServer.enqueue(
			MockResponse()
				.setResponseCode(500)
				.setBody("{\"error\": \"Internal Server Error\"}"),
		)

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onError = { error ->
					errorCallCount++
					capturedError = error
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for error callback
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Error callback should be invoked within timeout", completed)
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError?.message?.contains("500") ?: false,
			"Error message should contain '500'",
		)
	}

	@Test
	fun testInvalidJsonInvokesOnErrorExactlyOnce() {
		// Arrange
		val latch = CountDownLatch(1)
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Enqueue invalid JSON response
		mockWebServer.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody("{ invalid json }"),
		)

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onError = { error ->
					errorCallCount++
					capturedError = error
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for error callback
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Error callback should be invoked within timeout", completed)
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
	}

	@Test
	fun testEmptyResponseInvokesOnErrorExactlyOnce() {
		// Arrange
		val latch = CountDownLatch(1)
		var errorCallCount = 0
		var loadingCallCount = 0
		var capturedError: PayPalErrors.Base? = null

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Enqueue empty response (missing required fields)
		mockWebServer.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody("{}"),
		)

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onError = { error ->
					errorCallCount++
					capturedError = error
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for error callback
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Error callback should be invoked within timeout", completed)
		assertEquals(1, errorCallCount, "onError should be called exactly once")
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertNotNull(capturedError, "Error should be captured")
		assertTrue(
			capturedError is PayPalErrors.InvalidResponseException,
			"Error should be InvalidResponseException",
		)
	}

	@Test
	fun testLoadingAlwaysTransitionsToTerminalState() {
		// Arrange
		val latch = CountDownLatch(1)
		var loadingCallCount = 0
		var terminalStateReached = false

		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.DEVELOP(mockServerPort)

		// Enqueue error response
		mockWebServer.enqueue(
			MockResponse()
				.setResponseCode(503)
				.setBody("{\"error\": \"Service Unavailable\"}"),
		)

		val config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.DEVELOP(mockServerPort),
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = {
					loadingCallCount++
				},
				onSuccess = {
					terminalStateReached = true
					latch.countDown()
				},
				onError = {
					terminalStateReached = true
					latch.countDown()
				},
			),
		)

		// Act
		val messageView = PayPalMessageView(context, config = config)

		// Wait for terminal state
		val completed = latch.await(5, TimeUnit.SECONDS)

		// Assert
		assertTrue("Terminal state should be reached within timeout", completed)
		assertEquals(1, loadingCallCount, "onLoading should be called exactly once")
		assertTrue(
			terminalStateReached,
			"A terminal state (success or error) should be reached",
		)
	}
}
