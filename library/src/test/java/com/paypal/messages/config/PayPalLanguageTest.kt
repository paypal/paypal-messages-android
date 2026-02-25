package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for PayPalLanguage enum
 */
class PayPalLanguageTest {

	@Test
	fun testEnumProperties() {
		// Test a few representative enum values to verify structure
		assertEquals("en-US", PayPalLanguage.US_ENGLISH.code)
		assertEquals("fr-FR", PayPalLanguage.FRANCE.code)
		assertEquals("de-DE", PayPalLanguage.GERMANY.code)
	}

	@Test
	fun testLanguageValues() {
		// Test that enum values can be retrieved
		val values = PayPalLanguage.values()
		
		assertNotNull(values)
		assertEquals(10, values.size)
		assertEquals(PayPalLanguage.US_ENGLISH, values[0])
		assertEquals(PayPalLanguage.GB_ENGLISH, values[1])
		assertEquals(PayPalLanguage.AUSTRALIA_ENGLISH, values[2])
		assertEquals(PayPalLanguage.CANADA_ENGLISH, values[3])
		assertEquals(PayPalLanguage.CANADA_FRENCH, values[4])
		assertEquals(PayPalLanguage.SPAIN, values[5])
		assertEquals(PayPalLanguage.FRANCE, values[6])
		assertEquals(PayPalLanguage.GERMANY, values[7])
		assertEquals(PayPalLanguage.AUSTRIA, values[8])
		assertEquals(PayPalLanguage.ITALY, values[9])
	}

	@Test
	fun testLanguageValueOf() {
		// Test that valueOf works correctly
		assertEquals(PayPalLanguage.US_ENGLISH, PayPalLanguage.valueOf("US_ENGLISH"))
		assertEquals(PayPalLanguage.FRANCE, PayPalLanguage.valueOf("FRANCE"))
	}

	@Test
	fun testFromCodeValidCodes() {
		// Test fromCode with exact matches
		assertEquals(PayPalLanguage.US_ENGLISH, PayPalLanguage.fromCode("en-US"))
		assertEquals(PayPalLanguage.GB_ENGLISH, PayPalLanguage.fromCode("en-GB"))
		assertEquals(PayPalLanguage.AUSTRALIA_ENGLISH, PayPalLanguage.fromCode("en-AU"))
		assertEquals(PayPalLanguage.CANADA_ENGLISH, PayPalLanguage.fromCode("en-CA"))
		assertEquals(PayPalLanguage.CANADA_FRENCH, PayPalLanguage.fromCode("fr-CA"))
		assertEquals(PayPalLanguage.SPAIN, PayPalLanguage.fromCode("es-ES"))
		assertEquals(PayPalLanguage.FRANCE, PayPalLanguage.fromCode("fr-FR"))
		assertEquals(PayPalLanguage.GERMANY, PayPalLanguage.fromCode("de-DE"))
		assertEquals(PayPalLanguage.AUSTRIA, PayPalLanguage.fromCode("de-AT"))
		assertEquals(PayPalLanguage.ITALY, PayPalLanguage.fromCode("it-IT"))
	}

	@Test
	fun testFromCodeCaseInsensitive() {
		// Test that fromCode is case-insensitive
		assertEquals(PayPalLanguage.US_ENGLISH, PayPalLanguage.fromCode("EN-US"))
		assertEquals(PayPalLanguage.GB_ENGLISH, PayPalLanguage.fromCode("en-gb"))
		assertEquals(PayPalLanguage.FRANCE, PayPalLanguage.fromCode("FR-fr"))
		assertEquals(PayPalLanguage.GERMANY, PayPalLanguage.fromCode("DE-de"))
	}

	@Test
	fun testFromCodeInvalidCode() {
		// Test that fromCode returns null for invalid codes
		assertNull(PayPalLanguage.fromCode("invalid"))
		assertNull(PayPalLanguage.fromCode("en"))
		assertNull(PayPalLanguage.fromCode("fr-BE"))
		assertNull(PayPalLanguage.fromCode(""))
	}

	@Test
	fun testFromCodeNull() {
		// Test that fromCode handles null input
		assertNull(PayPalLanguage.fromCode(null))
	}
}
