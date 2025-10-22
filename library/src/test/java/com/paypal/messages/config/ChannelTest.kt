package com.paypal.messages.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChannelTest {
	@Test
	fun testNative() {
		assertEquals(Channel.NATIVE.toString(), "NATIVE")
	}

	@Test
	fun testValueOf() {
		assertEquals(Channel.NATIVE, Channel.valueOf("NATIVE"))
	}

	@Test
	fun testValues() {
		val values = Channel.values()
		assertEquals(1, values.size)
		assertEquals(Channel.NATIVE, values[0])
	}
}
