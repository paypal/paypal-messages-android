package com.paypal.messages.logger

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackingEventTest {
	private val eventType = EventType.MESSAGE_CLICK
	private val renderDuration = 100
	private val requestDuration = 200
	private val pageViewLinkName = "test_link_name"
	private val pageViewLinkSource = "test_link_src"
	private val data = "test_data"
	private val errorName = "test_error_name"
	private val errorDescription = "test_error_description"

	private val trackingEvent = TrackingEvent(
		eventType = eventType,
		renderDuration = renderDuration,
		requestDuration = requestDuration,
		pageViewLinkName = pageViewLinkName,
		pageViewLinkSource = pageViewLinkSource,
		data = data,
		errorName = errorName,
		errorDescription = errorDescription,
	)

	@Test
	fun testConstructor() {
		assertEquals(eventType, trackingEvent.eventType)
		assertEquals(renderDuration, trackingEvent.renderDuration)
		assertEquals(requestDuration, trackingEvent.requestDuration)
		assertEquals(pageViewLinkName, trackingEvent.pageViewLinkName)
		assertEquals(pageViewLinkSource, trackingEvent.pageViewLinkSource)
		assertEquals(data, trackingEvent.data)
		assertEquals(errorName, trackingEvent.errorName)
		assertEquals(errorDescription, trackingEvent.errorDescription)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(trackingEvent)

		@Suppress("ktlint:standard:max-line-length")
		val expectedJson = """{"event_type":"MESSAGE_CLICK","render_duration":100,"request_duration":200,"link_name":"test_link_name","link_src":"test_link_src","data":"test_data","error_name":"test_error_name","error_description":"test_error_description"}"""
		assertEquals(expectedJson, json)
	}
}
