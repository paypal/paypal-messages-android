package com.paypal.messages.analytics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentTypeTest {
	@Test
	fun testModal() {
		assertEquals(ComponentType.MODAL.toString(), "modal")
	}

	@Test
	fun testMessage() {
		assertEquals(ComponentType.MESSAGE.toString(), "message")
	}
}
