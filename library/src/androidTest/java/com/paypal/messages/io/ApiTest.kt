package com.paypal.messages.io

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageStyle
import com.paypal.messages.io.Api.preventEmptyValues
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig

@RunWith(AndroidJUnit4::class)
class ApiTest {
	private lateinit var context: Context
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var mockWebServer: MockWebServer
	private val instanceId = UUID.randomUUID()
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
		val messageDataRequest = Api.createMessageDataRequest(messageConfig, null, instanceId)
		val url = messageDataRequest.url.toString()

		val expectedPath = "credit-presentment/native/message"
		assertTrue(
			"url does not contain expectedPath value: $expectedPath",
			url.contains(expectedPath),
		)

		val expectedQueryParts = arrayOf(
			"client_id=test_client_id",
			"instance_id",
		)
		expectedQueryParts.forEach { assertTrue("url does not contain $it", url.contains(it)) }

		val notExpectedQueryParts = arrayOf(
			"dev_touchpoint=false",
			"ignore_cache=false",
		)
		notExpectedQueryParts.forEach { assertFalse("url contains $it", url.contains(it)) }
	}

	@Test
	fun testCreateMessageDataRequestWithAllData() {
		Api.sessionId = UUID.randomUUID()
		val config = MessageConfig(
			data = PayPalMessageData(
				clientID = "test_client_id",
				amount = 1.0,
				buyerCountry = "US",
				offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			),
			style = PayPalMessageStyle(),
		)
		val messageDataRequest = Api.createMessageDataRequest(config, "hash", instanceId)
		val url = messageDataRequest.url.toString()

		val expectedPath = "credit-presentment/native/message"
		assertTrue(
			"url does not contain expectedPath value: $expectedPath",
			url.contains(expectedPath),
		)

		val expectedQueryParts = arrayOf(
			"client_id=test_client_id",
			"instance_id",
			"session_id",
			"amount=1.0",
			"buyer_country=US",
			"offer=PAY_LATER_PAY_IN_1",
			"merchant_config=hash",
		)
		expectedQueryParts.forEach { assertTrue("url does not contain $it", url.contains(it)) }
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
			val result = Api.callMessageDataEndpoint(messageConfig, null, instanceId)
			assertTrue(
				"result is not ApiResult.Failure",
				result is ApiResult.Failure<*>,
			)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(
				"error is not FailedToFetchDataException",
				error is PayPalErrors.FailedToFetchDataException,
			)
			assertTrue(
				"""error message does not contain "Code was 404"""",
				error?.message?.contains("Code was 404") ?: false,
			)
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
			val result = Api.callMessageDataEndpoint(messageConfig, null, instanceId)
			assertTrue(
				"result is not ApiResult.Failure",
				result is ApiResult.Failure<*>,
			)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(
				"error is not InvalidResponseException",
				error is PayPalErrors.InvalidResponseException,
			)
			assertTrue(
				"""error message does not contain 12345""",
				error?.message?.contains("12345") ?: false,
			)
			assertTrue(
				"""error message does not contain Invalid Response""",
				error?.message?.contains("Invalid Response") ?: false,
			)
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
			val result = Api.callMessageDataEndpoint(messageConfig, null, instanceId)
			assertTrue(
				"result is not ApiResult.Success",
				result is ApiResult.Success<*>,
			)
			val response = (result as ApiResult.Success<*>).response
			assertTrue(
				"response contains content",
				response.toJson().contains("content"),
			)
			assertTrue(
				"response contains meta",
				response.toJson().contains("meta"),
			)
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
				assertNotNull("result is null", result)
				assertTrue(
					"result is not ApiResult.Success",
					result is ApiResult.Success<*>,
				)
				val data = (result as ApiResult.Success<*>).response as ApiMessageData.Response
				assertNotNull("content is null", data.content)
				assertNotNull("meta is null", data.meta)
			}
		}

		launch {
			Api.getMessageWithHash(context, messageConfig, instanceId, onActionCompleted)
		}.join()

		advanceUntilIdle()
	}

	@Test
	fun testCreateMessageHashRequest() {
		val request = Api.createMessageHashRequest("test_client_id")
		val url = request.url.toString()

		val expectedPath = "credit-presentment/merchant-profile"
		val expectedQueryParts = arrayOf("client_id=test_client_id")
		assertTrue(
			"url does not contain expectedPath: $expectedPath",
			request.url.toString().contains(expectedPath),
		)
		expectedQueryParts.forEach { assertTrue("url does not contain $it", url.contains(it)) }
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
			assertTrue(
				"result is not ApiResult.Failure",
				result is ApiResult.Failure<*>,
			)
			val error = (result as ApiResult.Failure<*>).error
			assertTrue(
				"error is not InvalidResponseException",
				error is PayPalErrors.InvalidResponseException,
			)
			assertTrue(
				"""error message does not contain 12345""",
				error?.message?.contains("12345") ?: false,
			)
			assertTrue(
				"""error message does not contain Invalid Response""",
				error?.message?.contains("Invalid Response") ?: false,
			)
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
			assertTrue(
				"result is not ApiResult.Success",
				result is ApiResult.Success<*>,
			)
			val response = (result as ApiResult.Success<*>).response
			val expectedParts = arrayOf(
				"cache_flow_disabled",
				"ttl_soft",
				"ttl_hard",
				"merchant_profile",
				"hash",
			)
			expectedParts.forEach {
				assertTrue(
					"response does not contain $it",
					response.toJson().contains(it),
				)
			}
		}
	}

	@Test
	fun testCreateModalUrl() {
		val url = Api.createModalUrl(
			clientId = "test_client_id",
			amount = 1.0,
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
		)

		val expectedPath = "credit-presentment/lander/modal"
		val expectedQueryParts = arrayOf(
			"client_id=",
			"integration_type=NATIVE_ANDROID",
			"features=native-modal",
			"amount=1.0",
			"buyer_country=US",
			"offer=PAY_LATER_PAY_IN_1",
		)

		assertTrue("url does not contain expectedPath: $expectedPath", url.contains(expectedPath))
		expectedQueryParts.forEach { assertTrue("url does not contain $it", url.contains(it)) }
	}

	@Test
	fun testCreateLoggerRequest() {
		val request = Api.createLoggerRequest("{}")
		val expectedPath = "v1/credit/upstream-messaging-events"
		assertTrue(request.url.toString().contains(expectedPath))
	}

	@Test
	fun testEmptyValuesInSerialization() {
		val payloadJson = """
		{
			"data":{
				"client_id":"",
				"components":[
					{
						"__shared__":{},
						"amount":"null",
						"channel":"NATIVE",
						"component_events":[],
						"style_color":"BLACK",
						"style_logo_type":"PRIMARY",
						"style_text_align":"LEFT",
						"type":"MESSAGE"
					}
				],
				"device_id":"android_id",
				"integration_type":"NATIVE_ANDROID",
				"integration_name": "",
				"integration_version": ""
			}
		}
		""".trimIndent()

		val updatedPayload = preventEmptyValues(payloadJson)

		assertNotEquals(payloadJson, updatedPayload)
		val notExpectedParts = arrayOf(
			"integration_name",
			"integration_version",
			"__shared__",
			"amount",
			"component_events",
		)
		notExpectedParts.forEach {
			assertFalse("updatedPayload contains $it", updatedPayload.contains(it))
		}
	}
}
