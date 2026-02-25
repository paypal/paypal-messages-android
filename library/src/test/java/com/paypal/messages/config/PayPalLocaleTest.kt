package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for PayPalLocale enum
 */
class PayPalLocaleTest {

	@Test
	fun testEnumProperties() {
		// Test a few representative enum values to verify structure
		assertEquals("en_US", PayPalLocale.US_ENGLISH.code)
		assertEquals("fr_FR", PayPalLocale.FRANCE.code)
		assertEquals("de_DE", PayPalLocale.GERMANY.code)
	}

	@Test
	fun testLocaleValues() {
		// Test that enum values can be retrieved
		val values = PayPalLocale.values()
		
		assertNotNull(values)
		assertEquals(10, values.size)
		assertEquals(PayPalLocale.US_ENGLISH, values[0])
		assertEquals(PayPalLocale.GB_ENGLISH, values[1])
		assertEquals(PayPalLocale.AUSTRALIA_ENGLISH, values[2])
		assertEquals(PayPalLocale.CANADA_ENGLISH, values[3])
		assertEquals(PayPalLocale.CANADA_FRENCH, values[4])
		assertEquals(PayPalLocale.SPAIN, values[5])
		assertEquals(PayPalLocale.FRANCE, values[6])
		assertEquals(PayPalLocale.GERMANY, values[7])
		assertEquals(PayPalLocale.AUSTRIA, values[8])
		assertEquals(PayPalLocale.ITALY, values[9])
	}

	@Test
	fun testLocaleValueOf() {
		// Test that valueOf works correctly
		assertEquals(PayPalLocale.US_ENGLISH, PayPalLocale.valueOf("US_ENGLISH"))
		assertEquals(PayPalLocale.FRANCE, PayPalLocale.valueOf("FRANCE"))
	}

	@Test
	fun testFromCodeValidCodes() {
		// Test fromCode with exact matches
		assertEquals(PayPalLocale.US_ENGLISH, PayPalLocale.fromCode("en_US"))
		assertEquals(PayPalLocale.GB_ENGLISH, PayPalLocale.fromCode("en_GB"))
		assertEquals(PayPalLocale.AUSTRALIA_ENGLISH, PayPalLocale.fromCode("en_AU"))
		assertEquals(PayPalLocale.CANADA_ENGLISH, PayPalLocale.fromCode("en_CA"))
		assertEquals(PayPalLocale.CANADA_FRENCH, PayPalLocale.fromCode("fr_CA"))
		assertEquals(PayPalLocale.SPAIN, PayPalLocale.fromCode("es_ES"))
		assertEquals(PayPalLocale.FRANCE, PayPalLocale.fromCode("fr_FR"))
		assertEquals(PayPalLocale.GERMANY, PayPalLocale.fromCode("de_DE"))
		assertEquals(PayPalLocale.AUSTRIA, PayPalLocale.fromCode("de_AT"))
		assertEquals(PayPalLocale.ITALY, PayPalLocale.fromCode("it_IT"))
	}

	@Test
	fun testFromCodeCaseInsensitive() {
		// Test that fromCode is case-insensitive
		assertEquals(PayPalLocale.US_ENGLISH, PayPalLocale.fromCode("EN_US"))
		assertEquals(PayPalLocale.GB_ENGLISH, PayPalLocale.fromCode("en_gb"))
		assertEquals(PayPalLocale.FRANCE, PayPalLocale.fromCode("FR_fr"))
		assertEquals(PayPalLocale.GERMANY, PayPalLocale.fromCode("DE_de"))
	}

	@Test
	fun testFromCodeInvalidCode() {
		// Test that fromCode returns null for invalid codes
		assertNull(PayPalLocale.fromCode("invalid"))
		assertNull(PayPalLocale.fromCode("en"))
		assertNull(PayPalLocale.fromCode("fr_BE"))
		assertNull(PayPalLocale.fromCode(""))
	}

	@Test
	fun testFromCodeNull() {
		// Test that fromCode handles null input
		assertNull(PayPalLocale.fromCode(null))
	}
}
