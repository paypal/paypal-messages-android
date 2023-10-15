package com.paypal.messages.io

import com.paypal.messages.utils.PayPalErrors
import okhttp3.Headers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiResultTest {
	@Test
	fun testSuccessToJson() {
		val success = ApiResult.Success(
			ApiHashData.Response(null, null, null, null),
		)

		val json = success.response.toJson()

		assertEquals("{}", json)
	}

	@Test
	fun testFailureToJson() {
		val failure = ApiResult.Failure(PayPalErrors.InvalidResponseException("test_message", "test_debug_id"))

		assertTrue(failure.error.message?.contains("test_message") ?: false)
		assertEquals("test_debug_id", failure.error.debugId)
	}

	@Test
	fun testGetFailureWithDebugId() {
		val headers = Headers.Builder()
			.add("Paypal-Debug-Id", "test_debug_id")
			.build()

		val failure = ApiResult.getFailureWithDebugId(headers) as ApiResult.Failure<*>

		assertEquals("test_debug_id", failure.error?.debugId)
	}
}
