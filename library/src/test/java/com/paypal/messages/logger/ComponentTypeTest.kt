package com.paypal.messages.logger

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
