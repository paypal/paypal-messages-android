package com.paypal.messages.analytics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventTypeTest {
	@Test
	fun testMessageRender() {
		assertEquals(EventType.MESSAGE_RENDERED.toString(), "message_rendered")
	}

	@Test
	fun testMessageClick() {
		assertEquals(EventType.MESSAGE_CLICKED.toString(), "message_clicked")
	}

	@Test
	fun testModalRender() {
		assertEquals(EventType.MODAL_RENDERED.toString(), "modal_rendered")
	}

	@Test
	fun testModalClick() {
		assertEquals(EventType.MODAL_CLICKED.toString(), "modal_clicked")
	}

	@Test
	fun testModalOpen() {
		assertEquals(EventType.MODAL_OPENED.toString(), "modal_opened")
	}

	@Test
	fun testModalClose() {
		assertEquals(EventType.MODAL_CLOSED.toString(), "modal_closed")
	}

	@Test
	fun testModalError() {
		assertEquals(EventType.MODAL_ERROR.toString(), "modal_error")
	}
}
