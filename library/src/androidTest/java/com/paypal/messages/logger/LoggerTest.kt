package com.paypal.messages.logger

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoggerTest {
	private lateinit var context: Context
	private lateinit var logger: Logger
	private lateinit var component: TrackingComponent

	@Before
	fun setUp() {
		context = InstrumentationRegistry.getInstrumentation().targetContext
		val sharedPreferences = context.getSharedPreferences("com.paypal.messages", Context.MODE_PRIVATE)
		sharedPreferences.edit().putString("merchantHash", "1234567890").apply()

		logger = Logger.getInstance("test_client_id")
		component = TrackingComponent(
			instanceId = "test_instance_id",
			type = "test_type",
			componentEvents = mutableListOf(),
		)
	}

	@Test
	fun testGetInstance() {
		assertNotNull(logger)
	}

	@Test
	fun testSetGlobalAnalytics() {
		logger.setGlobalAnalytics("test_integration_name", "test_integration_version")

		assertEquals("test_integration_name", logger.integrationName)
		assertEquals("test_integration_version", logger.integrationVersion)
	}

	// TODO: Figure out why this test passes locally but fails in the CI
	@Test
	fun testLog() {
		component.componentEvents.add(TrackingEvent(EventType.MESSAGE_CLICK))
		logger.setGlobalAnalytics("test_integration_name", "test_integration_version")
		logger.log(context, component)

		val payload = logger.payload
		assertNotNull(payload)
		if (payload != null) {
			assertEquals("test_client_id", payload.clientId)
			assertEquals("random_session_id", payload.sessionId)
			assertEquals("test_integration_name", payload.integrationName)
			assertEquals("test_integration_version", payload.integrationVersion)

			assertEquals(1, payload.components.size)
			assertEquals("test_type", payload.components[0].type)
			assertEquals("test_instance_id", payload.components[0].instanceId)
			assertEquals(1, payload.components[0].componentEvents.size)
			assertEquals(EventType.MESSAGE_CLICK, payload.components[0].componentEvents[0].eventType)
		}
	}
}
