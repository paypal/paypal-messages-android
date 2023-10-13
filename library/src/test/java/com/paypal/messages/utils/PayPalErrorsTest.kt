package com.paypal.messages.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayPalErrorsTest {

	@Test
	fun testBaseException() {
		val exception = PayPalErrors.Base("test_message", "test_id")

		assertEquals("PayPalMessageException\n  debugId: test_id\n  message: test_message", exception.message)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testFailedToFetchDataException() {
		val exception = PayPalErrors.FailedToFetchDataException("test_message", "test_id")

		assertTrue(
			exception.message?.contains("Failed to get Message Data: test_message") ?: false,
		)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testIllegalEnumArgException() {
		val exception = PayPalErrors.IllegalEnumArg("enum_name", 10)

		assertTrue(
			exception.message?.contains(
				"Attempted to create a enum_name with an invalid index. " +
					"Please use an index that is between 0 and 9 and try again.",
			) ?: false,
			exception.message,
		)
		assertEquals(null, exception.debugId)
	}

	@Test
	fun testInvalidClientIdException() {
		val exception = PayPalErrors.InvalidClientIdException("test_message", "test_id")

		assertTrue(exception.message?.contains("Invalid ClientID: test_message") ?: false)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testInvalidResponseException() {
		val exception = PayPalErrors.InvalidResponseException("test_message", "test_id")

		assertTrue(exception.message?.contains("Invalid Response: test_message") ?: false)
		assertEquals("test_id", exception.debugId)
	}

	@Test
	fun testModalFailedToLoadException() {
		val exception = PayPalErrors.ModalFailedToLoad("test_message", "test_id")

		assertTrue(exception.message?.contains("Modal failed to open: test_message") ?: false)
		assertEquals("test_id", exception.debugId)
	}
}
