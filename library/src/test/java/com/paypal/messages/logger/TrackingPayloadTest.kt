package com.paypal.messages.logger

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackingPayloadTest {
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

	private val trackingPayload = TrackingPayload(
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
		specversion = "1.0",
		type = "com.paypal.credit.upstream-presentment.v1",
		source = "urn:paypal:event-src:v1:android:messages",
		datacontenttype = "application/json",
		dataschema = "ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json",
		data = trackingPayload,
	)

	@Test
	fun testConstructor() {
		assertEquals(cloudWrappedEvent.specversion, "1.0")
		assertEquals(cloudWrappedEvent.type, "com.paypal.credit.upstream-presentment.v1")
		assertEquals(cloudWrappedEvent.source, "urn:paypal:event-src:v1:android:messages")
		assertEquals(cloudWrappedEvent.datacontenttype, "application/json")
		assertEquals(
			cloudWrappedEvent.dataschema,
			"ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json",
		)
		assertEquals(clientId, cloudWrappedEvent.data.clientId)
		assertEquals(merchantId, cloudWrappedEvent.data.merchantId)
		assertEquals(partnerAttributionId, cloudWrappedEvent.data.partnerAttributionId)
		assertEquals(merchantProfileHash, cloudWrappedEvent.data.merchantProfileHash)
		assertEquals(deviceId, cloudWrappedEvent.data.deviceId)
		assertEquals(sessionId, cloudWrappedEvent.data.sessionId)
		assertEquals(instanceId, cloudWrappedEvent.data.instanceId)
		assertEquals(integrationName, cloudWrappedEvent.data.integrationName)
		assertEquals(integrationType, cloudWrappedEvent.data.integrationType)
		assertEquals(integrationVersion, cloudWrappedEvent.data.integrationVersion)
		assertEquals(libraryVersion, cloudWrappedEvent.data.libraryVersion)
		assertEquals(components, cloudWrappedEvent.data.components)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(trackingPayload)

		@Suppress("ktlint:standard:max-line-length")
		val expectedJson = """{"client_id":"test_client_id","merchant_id":"test_merchant_id","partner_attribution_id":"test_partner_attribution_id","merchant_profile_hash":"test_merchant_profile_hash","device_id":"test_device_id","session_id":"test_session_id","instance_id":"test_instance_id","integration_name":"test_integration_name","integration_type":"test_integration_type","integration_version":"test_integration_version","lib_version":"test_library_version","components":[]}"""
		assertEquals(expectedJson, json)
	}
}
