package com.paypal.messages.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayPalErrorsTest {
	@Test
	fun testBaseException() {
		// Provides coverage for the wrapping object
		assertEquals(PayPalErrors, PayPalErrors)

		val exception = PayPalErrors.Base("test_message", "test_id")

		assertEquals("PayPalMessageException\n  debugId: test_id\n  message: test_message", exception.message)
		assertEquals("test_id", exception.debugId)

		val exceptionNoId = PayPalErrors.Base("test_message")

		assertEquals("PayPalMessageException\n  debugId: null\n  message: test_message", exceptionNoId.message)
		assertEquals(null, exceptionNoId.debugId)
	}

	@Test
	fun testFailedToFetchDataException() {
		val exception = PayPalErrors.FailedToFetchDataException("test_message", "test_id")

		assertTrue(
			exception.message?.contains("Failed to get Message Data: test_message") ?: false,
			"FailedToFetchDataException with ID does not contain expected message",
		)
		assertEquals("test_id", exception.debugId)

		val exceptionNoId = PayPalErrors.FailedToFetchDataException("test_message")

		assertTrue(
			exceptionNoId.message?.contains("Failed to get Message Data: test_message") ?: false,
			"FailedToFetchDataException with no ID does not contain expected message",
		)
		assertEquals(null, exceptionNoId.debugId)
	}

	@Test
	fun testIllegalEnumArgException() {
		val exception = PayPalErrors.IllegalEnumArg("enum_name", 10)

		assertTrue(
			exception.message?.contains(
				"Attempted to create a enum_name with an invalid index. " +
					"Please use an index that is between 0 and 9 and try again.",
			) ?: false,
			"IllegalEnumArgException does not contain expected message",
		)
		assertEquals(null, exception.debugId)
	}

	@Test
	fun testInvalidClientIdException() {
		val exception = PayPalErrors.InvalidClientIdException("test_message", "test_id")

		assertTrue(
			exception.message?.contains("Invalid ClientID: test_message") ?: false,
			"InvalidClientIdException with ID does not contain expected message",
		)
		assertEquals("test_id", exception.debugId)

		val exceptionNoId = PayPalErrors.InvalidClientIdException("test_message")

		assertTrue(
			exceptionNoId.message?.contains("Invalid ClientID: test_message") ?: false,
			"InvalidClientIdException with no ID does not contain expected message",
		)
		assertEquals(null, exceptionNoId.debugId)
	}

	@Test
	fun testInvalidResponseException() {
		val exception = PayPalErrors.InvalidResponseException("test_message", "test_id")

		assertTrue(
			exception.message?.contains("Invalid Response: test_message") ?: false,
			"InvalidResponseException with ID does not contain expected message",
		)
		assertEquals("test_id", exception.debugId)

		val exceptionNoId = PayPalErrors.InvalidResponseException("test_message")

		assertTrue(
			exceptionNoId.message?.contains("Invalid Response: test_message") ?: false,
			"InvalidResponseException with no ID does not contain expected message",
		)
		assertEquals(null, exceptionNoId.debugId)
	}

	@Test
	fun testModalFailedToLoadException() {
		val exception = PayPalErrors.ModalFailedToLoad("test_message", "test_id")

		assertTrue(
			exception.message?.contains("Modal failed to open: test_message") ?: false,
			"ModalFailedToLoadException with ID does not contain expected message",
		)
		assertEquals("test_id", exception.debugId)

		val exceptionNoId = PayPalErrors.ModalFailedToLoad("test_message")

		assertTrue(
			exceptionNoId.message?.contains("Modal failed to open: test_message") ?: false,
			"ModalFailedToLoadException with no ID does not contain expected message",
		)
		assertEquals(null, exceptionNoId.debugId)
	}
}
