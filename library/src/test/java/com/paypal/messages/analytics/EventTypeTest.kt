package com.paypal.messages.analytics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventTypeTest {
	@Test
	fun testMessageRender() {
		assertEquals(EventType.MESSAGE_RENDER.toString(), "MESSAGE_RENDER")
	}

	@Test
	fun testMessageClick() {
		assertEquals(EventType.MESSAGE_CLICK.toString(), "MESSAGE_CLICK")
	}

	@Test
	fun testModalRender() {
		assertEquals(EventType.MODAL_RENDER.toString(), "MODAL_RENDER")
	}

	@Test
	fun testModalClick() {
		assertEquals(EventType.MODAL_CLICK.toString(), "MODAL_CLICK")
	}

	@Test
	fun testModalOpen() {
		assertEquals(EventType.MODAL_OPEN.toString(), "MODAL_OPEN")
	}

	@Test
	fun testModalClose() {
		assertEquals(EventType.MODAL_CLOSE.toString(), "MODAL_CLOSE")
	}

	@Test
	fun testModalError() {
		assertEquals(EventType.MODAL_ERROR.toString(), "MODAL_ERROR")
	}
}
