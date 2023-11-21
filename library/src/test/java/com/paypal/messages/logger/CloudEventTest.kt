package com.paypal.messages.logger

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CloudEventTest {
	private val clientId = "test_client_id"
	private val merchantId = "test_merchant_id"
	private val partnerAttributionId = "test_partner_attribution_id"
	private val merchantProfileHash = "test_merchant_profile_hash"
	private val deviceId = "test_device_id"
	private val sessionId = "test_session_id"
	private val instanceId = "test_instance_id"
	private val integrationName = "test_integration_name"
	private val integrationType = "test_integration_type"
	private val integrationVersion = "test_integration_version"
	private val libraryVersion = "test_library_version"
	private val components = mutableListOf<TrackingComponent>()
	private val specversion = "1.0"
	private val type = "com.paypal.credit.upstream-presentment.v1"
	private val source = "urn:paypal:event-src:v1:android:messages"
	private val datacontenttype = "application/json"

	@Suppress("ktlint:standard:max-line-length")
	private val dataschema = "ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json"

	private val data = TrackingPayload(
		clientId = clientId,
		merchantId = merchantId,
		partnerAttributionId = partnerAttributionId,
		merchantProfileHash = merchantProfileHash,
		deviceId = deviceId,
		sessionId = sessionId,
		instanceId = instanceId,
		integrationName = integrationName,
		integrationType = integrationType,
		integrationVersion = integrationVersion,
		libraryVersion = libraryVersion,
		components = components,
	)

	private val cloudWrappedEvent = CloudEvent(
		specversion = specversion,
		type = type,
		source = source,
		datacontenttype = datacontenttype,
		dataschema = dataschema,
		data = data,
	)

	@Test
	fun testConstructor() {
		assertEquals(cloudWrappedEvent.specversion, specversion)
		assertEquals(cloudWrappedEvent.type, type)
		assertEquals(cloudWrappedEvent.source, source)
		assertEquals(cloudWrappedEvent.datacontenttype, datacontenttype)
		assertEquals(cloudWrappedEvent.dataschema, dataschema)
		assertEquals(cloudWrappedEvent.data, data)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(cloudWrappedEvent)

		@Suppress("ktlint:standard:max-line-length")
		val expectedJson = """{"specversion":"1.0","id":"${cloudWrappedEvent.id}","type":"com.paypal.credit.upstream-presentment.v1","source":"urn:paypal:event-src:v1:android:messages","datacontenttype":"application/json","dataschema":"ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json","time":"${cloudWrappedEvent.time}","data":{"client_id":"test_client_id","merchant_id":"test_merchant_id","partner_attribution_id":"test_partner_attribution_id","merchant_profile_hash":"test_merchant_profile_hash","device_id":"test_device_id","session_id":"test_session_id","instance_id":"test_instance_id","integration_name":"test_integration_name","integration_type":"test_integration_type","integration_version":"test_integration_version","lib_version":"test_library_version","components":[]}}"""
		assertEquals(expectedJson, json)
	}
}
