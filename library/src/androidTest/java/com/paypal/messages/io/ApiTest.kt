package com.paypal.messages.io

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageStyle
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig

@RunWith(AndroidJUnit4::class)
class ApiTest {
	private lateinit var context: Context
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var mockWebServer: MockWebServer
	private val messageConfig = MessageConfig(
		data = PayPalMessageData(clientID = "test_client_id"),
	)

	@Suppress("ktlint:standard:max-line-length")
	private val merchantProfileHashData = """{"cache_flow_disabled":false,"merchant_profile":{"hash":"1234567891"},"ttl_hard":0,"ttl_soft":0}"""
	private val messageData = """{"meta": {}, "content": {}}"""

	@OptIn(ExperimentalCoroutinesApi::class)
	val standardTestDispatcher = StandardTestDispatcher()

	@Before
	fun setUp() {
		context = InstrumentationRegistry.getInstrumentation().targetContext
		val key = "PayPalUpstreamLocalStorage"
		sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
		sharedPreferences.edit().putString("merchantProfileHashData", merchantProfileHashData).apply()

		mockWebServer = MockWebServer()
		mockWebServer.start()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@After
	fun tearDown() {
		standardTestDispatcher.cancel()
	}

	@Test
	fun testCreateMessageDataRequestWithNoData() {
		val messageDataRequest = Api.createMessageDataRequest(messageConfig, null)

		val url = messageDataRequest.url.toString()
		LogCat.debug("API TEST", "no data url: $url")

		val expectedPath = "credit-presentment/native/message"
		val expectedQueryParts = arrayOf(
			"client_id=test_client_id",
			"devTouchpoint=false",
			"ignore_cache=false",
			"instance_id=null",
			"session_id=null",
		)

		assertTrue(url.contains(expectedPath))
		expectedQueryParts.forEach {
			assertTrue(url.contains(it))
		}
	}

	@Test
	fun testCreateMessageDataRequestWithAllData() {
		val config = MessageConfig(
			data = PayPalMessageData(
				clientID = "test_client_id",
				amount = 1.0,
				buyerCountry = "US",
				offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			),
			style = PayPalMessageStyle(),
		)
		val messageDataRequest = Api.createMessageDataRequest(config, "hash")

		val url = messageDataRequest.url.toString()
		LogCat.debug("API TEST", "all data url: $url")
		val expectedPath = "credit-presentment/native/message"
		val expectedQueryParts = arrayOf(
			"client_id=test_client_id",
			"devTouchpoint=false",
			"ignore_cache=false",
			"instance_id=null",
			"session_id=null",
			"amount=1.0",
			"buyer_country=US",
			"offer=PAY_LATER_PAY_IN_1",
			"merchant_config=hash"
		)

		assertTrue(url.contains(expectedPath))
		expectedQueryParts.forEach {
			assertTrue(url.contains(it))
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testCallMessageDataEndpointWith404() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockMessageDataResponse = MockResponse()
			.setResponseCode(404)
			.setBody(messageData)
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockMessageDataResponse)

		launch {
			val result = Api.callMessageDataEndpoint(messageConfig, null)
			assertTrue(result is ApiResult.Failure<*>)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(error is PayPalErrors.FailedToFetchDataException)
			assertTrue(error?.message?.contains("Code was 404") ?: false)
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testCallMessageDataEndpointWithBadData() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockMessageDataResponse = MockResponse()
			.setResponseCode(200)
			.setBody("{}")
			.addHeader("Paypal-Debug-Id", "12345")
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockMessageDataResponse)

		launch {
			val result = Api.callMessageDataEndpoint(messageConfig, null)
			assertTrue(result is ApiResult.Failure<*>)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(error is PayPalErrors.InvalidResponseException)
			assertTrue(error?.message?.contains("12345") ?: false)
			assertTrue(error?.message?.contains("Invalid Response") ?: false)
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testCallMessageDataEndpointWithGoodData() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockMessageDataResponse = MockResponse()
			.setResponseCode(200)
			.setBody(messageData)
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockMessageDataResponse)

		launch {
			val result = Api.callMessageDataEndpoint(messageConfig, null)
			assertTrue(result is ApiResult.Success<*>)
			val response = (result as ApiResult.Success<*>).response
			assertTrue(response.toJson().contains("content"))
			assertTrue(response.toJson().contains("meta"))
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testGetMessageWithHash() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.ioDispatcher = standardTestDispatcher
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockMerchantProfileResponse = MockResponse()
			.setBody(merchantProfileHashData)
			.addHeader("Content-Type", "application/json")
		val mockMessageDataResponse = MockResponse()
			.setBody(messageData)
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockMerchantProfileResponse)
		mockWebServer.enqueue(mockMessageDataResponse)

		val onActionCompleted = object : OnActionCompleted {
			override fun onActionCompleted(result: ApiResult) {
				assertNotNull(result)
				assertTrue(result is ApiResult.Success<*>)
				val data = (result as ApiResult.Success<*>).response as ApiMessageData.Response
				assertNotNull(data.content)
				assertNotNull(data.meta)
			}
		}

		launch {
			Api.getMessageWithHash(context, messageConfig, onActionCompleted)

			this.cancel()
		}

		advanceUntilIdle()
	}

	@Test
	fun testCreateMessageHashRequest() {
		val request = Api.createMessageHashRequest("test_client_id")

		@Suppress("ktlint:standard:max-line-length")
		val expectedPath = "credit-presentment/merchant-profile?client_id=test_client_id"
		assertTrue(request.url.toString().contains(expectedPath))
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testCallMessageHashEndpointWithFailure() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockHashDataResponse = MockResponse()
			.setResponseCode(404)
			.setBody(merchantProfileHashData)
			.addHeader("Paypal-Debug-Id", "12345")
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockHashDataResponse)

		launch {
			val result = Api.callMessageHashEndpoint("test_client_id")
			assertTrue(result is ApiResult.Failure<*>)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(error is PayPalErrors.InvalidResponseException)
			assertTrue(error?.message?.contains("12345") ?: false)
			assertTrue(error?.message?.contains("Invalid Response") ?: false)
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun testCallMessageHashEndpointWithSuccess() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port
		Api.env = PayPalEnvironment.local(mockServerPort)

		val mockHashDataResponse = MockResponse()
			.setResponseCode(200)
			.setBody(merchantProfileHashData)
			.addHeader("Content-Type", "application/json")
		mockWebServer.enqueue(mockHashDataResponse)

		launch {
			val result = Api.callMessageHashEndpoint("test_client_id")
			assertTrue(result is ApiResult.Success<*>)
			val response = (result as ApiResult.Success<*>).response
			LogCat.debug("TEST", response.toJson())
			assertTrue(response.toJson().contains("cache_flow_disabled"))
			assertTrue(response.toJson().contains("ttl_soft"))
			assertTrue(response.toJson().contains("ttl_hard"))
			assertTrue(response.toJson().contains("merchant_profile"))
			assertTrue(response.toJson().contains("hash"))
		}
	}

	@Test
	fun testCreateModalUrl() {
		val url = Api.createModalUrl(
			clientId = "",
			amount = 1.0,
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
		)

		@Suppress("ktlint:standard:max-line-length")
		val expectedPath = "credit-presentment/lander/modal?client_id=&integration_type=NATIVE_ANDROID&features=native-modal&amount=1.0&buyer_country=US&offer=PAY_LATER_PAY_IN_1"
		assertTrue(url.contains(expectedPath))
	}

	@Test
	fun testCreateLoggerRequest() {
		val request = Api.createLoggerRequest("{}")
		val expectedPath = "v1/credit/upstream-messaging-events"
		assertTrue(request.url.toString().contains(expectedPath))
	}
}
