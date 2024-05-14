package com.paypal.messages.analytics

import com.google.gson.Gson
import com.paypal.messages.extensions.getJsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CloudEventTest {
	private val clientId = "test_client_id"
	private val merchantId = "test_merchant_id"
	private val partnerAttributionId = "test_partner_attribution_id"
	private val merchantProfileHash = "test_merchant_profile_hash"
	private val instanceId = "test_instance_id"
	private val integrationName = "test_integration_name"
	private val integrationType = "test_integration_type"
	private val integrationVersion = "test_integration_version"
	private val libraryVersion = "test_library_version"
	private val components = mutableListOf<AnalyticsComponent>()
	private val specVersion = "1.0"
	private val type = "com.paypal.credit.upstream-presentment.v1"
	private val source = "urn:paypal:event-src:v1:android:messages"
	private val dataContentType = "application/json"

	@Suppress("ktlint:standard:max-line-length")
	private val dataSchema = "ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json"

	private val gson = Gson()
	private val data = gson.getJsonObject(
		AnalyticsPayload(
			clientId = clientId,
			merchantId = merchantId,
			partnerAttributionId = partnerAttributionId,
			merchantProfileHash = merchantProfileHash,
			instanceId = instanceId,
			integrationName = integrationName,
			integrationType = integrationType,
			integrationVersion = integrationVersion,
			libraryVersion = libraryVersion,
			components = components,
		),
	)

	private val cloudWrappedEvent = CloudEvent(
		specVersion = specVersion,
		type = type,
		source = source,
		dataContentType = dataContentType,
		dataSchema = dataSchema,
		data = data,
	)

	@Test
	fun testConstructor() {
		assertEquals(cloudWrappedEvent.specVersion, specVersion)
		assertEquals(cloudWrappedEvent.type, type)
		assertEquals(cloudWrappedEvent.source, source)
		assertEquals(cloudWrappedEvent.dataContentType, dataContentType)
		assertEquals(cloudWrappedEvent.dataSchema, dataSchema)
		assertEquals(cloudWrappedEvent.data, data)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(cloudWrappedEvent)

		val expectedParts = arrayOf(
			""""specversion":"1.0"""",
			""""id":"${cloudWrappedEvent.id}"""",
			""""type":"com.paypal.credit.upstream-presentment.v1"""",
			""""source":"urn:paypal:event-src:v1:android:messages"""",
			""""datacontenttype":"application/json"""",
			""""dataschema":"ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json"""",
			""""time":"${cloudWrappedEvent.time}"""",
			""""data":{"client_id":"test_client_id"""",
			""""merchant_id":"test_merchant_id"""",
			""""partner_attribution_id":"test_partner_attribution_id"""",
			""""merchant_profile_hash":"test_merchant_profile_hash"""",
			""""instance_id":"test_instance_id"""",
			""""integration_name":"test_integration_name"""",
			""""integration_type":"test_integration_type"""",
			""""integration_version":"test_integration_version"""",
			""""lib_version":"test_library_version"""",
			""""components":[]""",
		)

		expectedParts.forEach {
			assertTrue(it in json, "json does not contain $it")
		}
	}
}
