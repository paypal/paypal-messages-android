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

		logger = Logger.getInstance("clientId")
		component = TrackingComponent(
			instanceId = "test_instance_id",
			type = "test_type",
			events = mutableListOf(),
		)
	}

	@Test
	fun testGetInstance() {
		assertNotNull(logger)
	}

	@Test
	fun testSetGlobalAnalytics() {
		logger.setGlobalAnalytics("integrationName", "integrationVersion")

		assertEquals("integrationName", logger.integrationName)
		assertEquals("integrationVersion", logger.integrationVersion)
	}

	@Test
	fun testLog() {
		component.events.add(TrackingEvent(EventType.MESSAGE_CLICK))
		logger.setGlobalAnalytics("integrationName", "integrationVersion")
		logger.log(context, component)

		val payload = logger.payload
		assertNotNull(payload)
		if (payload != null) {
			assertEquals("clientId", payload.clientId)
			assertEquals("1234567890", payload.merchantProfileHash)
			assertEquals("random for now, TBD at later point to what this is specifically", payload.sessionId)
			assertEquals("integrationName", payload.integrationName)
			assertEquals("integrationVersion", payload.integrationVersion)

			assertEquals(1, payload.components.size)
			assertEquals("test_type", payload.components[0].type)
			assertEquals("test_instance_id", payload.components[0].instanceId)
			assertEquals(1, payload.components[0].events.size)
			assertEquals(EventType.MESSAGE_CLICK, payload.components[0].events[0].eventType)
		}
	}
}
