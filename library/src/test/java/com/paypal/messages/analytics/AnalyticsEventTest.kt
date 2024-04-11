package com.paypal.messages.analytics

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnalyticsEventTest {
	private val eventType = EventType.MESSAGE_CLICK
	private val renderDuration = "100"
	private val requestDuration = "200"
	private val pageViewLinkName = "test_link_name"
	private val pageViewLinkSource = "test_link_src"
	private val data = "test_data"
	private val errorName = "test_error_name"
	private val errorDescription = "test_error_description"

	private val analyticsEvent = AnalyticsEvent(
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
		assertEquals(eventType, analyticsEvent.eventType)
		assertEquals(renderDuration, analyticsEvent.renderDuration)
		assertEquals(requestDuration, analyticsEvent.requestDuration)
		assertEquals(pageViewLinkName, analyticsEvent.pageViewLinkName)
		assertEquals(pageViewLinkSource, analyticsEvent.pageViewLinkSource)
		assertEquals(data, analyticsEvent.data)
		assertEquals(errorName, analyticsEvent.errorName)
		assertEquals(errorDescription, analyticsEvent.errorDescription)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(analyticsEvent)

		@Suppress("ktlint:standard:max-line-length")
		val expectedJson = """{"event_type":"MESSAGE_CLICK","render_duration":100,"request_duration":200,"page_view_link_name":"test_link_name","page_view_link_source":"test_link_src","data":"test_data","error_name":"test_error_name","error_description":"test_error_description"}"""
		assertEquals(expectedJson, json)
	}
}
