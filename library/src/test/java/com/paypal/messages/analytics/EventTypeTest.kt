package com.paypal.messages.analytics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventTypeTest {
	@Test
	fun testMessageRender() {
		assertEquals(EventType.MESSAGE_RENDERED.toString(), "MESSAGE_RENDER")
	}

	@Test
	fun testMessageClick() {
		assertEquals(EventType.MESSAGE_CLICKED.toString(), "MESSAGE_CLICK")
	}

	@Test
	fun testModalRender() {
		assertEquals(EventType.MODAL_RENDERED.toString(), "MODAL_RENDER")
	}

	@Test
	fun testModalClick() {
		assertEquals(EventType.MODAL_CLICKED.toString(), "MODAL_CLICK")
	}

	@Test
	fun testModalOpen() {
		assertEquals(EventType.MODAL_OPENED.toString(), "MODAL_OPEN")
	}

	@Test
	fun testModalClose() {
		assertEquals(EventType.MODAL_CLOSED.toString(), "MODAL_CLOSE")
	}

	@Test
	fun testModalError() {
		assertEquals(EventType.MODAL_ERROR.toString(), "MODAL_ERROR")
	}
}
