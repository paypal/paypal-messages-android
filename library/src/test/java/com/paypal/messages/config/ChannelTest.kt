package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for Channel enum
 */
class ChannelTest {

	@Test
	fun testNativeChannel() {
		// Test that NATIVE enum value exists and is accessible
		val channel = Channel.NATIVE
		
		assertNotNull(channel)
		assertEquals("NATIVE", channel.name)
	}

	@Test
	fun testChannelValues() {
		// Test that enum values can be retrieved
		val values = Channel.values()
		
		assertNotNull(values)
		assertEquals(1, values.size)
		assertEquals(Channel.NATIVE, values[0])
	}

	@Test
	fun testChannelValueOf() {
		// Test that valueOf works correctly
		val channel = Channel.valueOf("NATIVE")
		
		assertNotNull(channel)
		assertEquals(Channel.NATIVE, channel)
	}

	@Test
	fun testChannelToString() {
		// Test toString representation
		val channel = Channel.NATIVE
		
		assertEquals("NATIVE", channel.toString())
	}
}
