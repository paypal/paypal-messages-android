package com.paypal.messages.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PayPalErrorsTest {

	@Test
	fun testBaseException() {
		val exception = PayPalErrors.Base("test_message", "paypal_debug_id")

		assertEquals("PayPalMessageException\n  debugId: paypal_debug_id\n  message: test_message", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testFailedToFetchDataException() {
		val exception = PayPalErrors.FailedToFetchDataException("test_message", "test_id")

		assertEquals("Failed to get Message Data", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testIllegalEnumArgException() {
		val exception = PayPalErrors.IllegalEnumArg("enum_name", 10)

		assertEquals(
			"Attempted to create a enum_name with an invalid index. " +
				"Please use an index that is between 0 and 9 and try again.",
			exception.message
		)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testInvalidCheckoutConfigException() {
		val exception = PayPalErrors.InvalidCheckoutConfigException("test_message", "test_id")

		assertEquals("Invalid Checkout Config", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testInvalidClientIdException() {
		val exception = PayPalErrors.InvalidClientIdException("test_message", "test_id")

		assertEquals("Invalid Client-ID", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testInvalidResponseException() {
		val exception = PayPalErrors.InvalidResponseException("test_message", "test_id")

		assertEquals("Invalid Response", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testModalFailedToLoadException() {
		val exception = PayPalErrors.ModalFailedToLoad("test_message", "test_id")

		assertEquals("Modal failed to open: test_message", exception.message)
		assertEquals("test_id", exception.debugId)
	}
}
