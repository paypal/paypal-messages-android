package com.paypal.messages.io

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageData
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

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun getMessageWithHash_noMerchantHash_fetchesNewHashAndReturnsData() = runTest(standardTestDispatcher) {
		val mockServerPort = mockWebServer.url("").port

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

		Api.ioDispatcher = standardTestDispatcher
		Api.env = PayPalEnvironment.local(mockServerPort)
		launch {
			Api.getMessageWithHash(context, messageConfig, onActionCompleted)
		}

		advanceUntilIdle()
	}
}
