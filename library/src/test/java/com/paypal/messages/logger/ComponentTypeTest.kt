package com.paypal.messages.logger

import org.junit.Assert.assertEquals
import org.junit.Test

class ComponentTypeTest {
	@Test
	fun testModal() {
		assertEquals(ComponentType.MODAL.toString(), "MODAL")
	}

	@Test
	fun testMessage() {
		assertEquals(ComponentType.MESSAGE.toString(), "MESSAGE")
	}
}
