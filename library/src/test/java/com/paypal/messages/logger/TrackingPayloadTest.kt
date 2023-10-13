package com.paypal.messages.logger

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

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
	
	@Test
	fun testConstructor() {
		assertEquals(clientId, trackingPayload.clientId)
		assertEquals(merchantId, trackingPayload.merchantId)
		assertEquals(partnerAttributionId, trackingPayload.partnerAttributionId)
		assertEquals(merchantProfileHash, trackingPayload.merchantProfileHash)
		assertEquals(deviceId, trackingPayload.deviceId)
		assertEquals(sessionId, trackingPayload.sessionId)
		assertEquals(instanceId, trackingPayload.instanceId)
		assertEquals(integrationName, trackingPayload.integrationName)
		assertEquals(integrationType, trackingPayload.integrationType)
		assertEquals(integrationVersion, trackingPayload.integrationVersion)
		assertEquals(libraryVersion, trackingPayload.libraryVersion)
		assertEquals(components, trackingPayload.components)
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(trackingPayload)
		
		assertEquals(json, "{\"client_id\":\"test_client_id\",\"merchant_id\":\"test_merchant_id\",\"partner_attribution_id\":\"test_partner_attribution_id\",\"merchant_profile_hash\":\"test_merchant_profile_hash\",\"device_id\":\"test_device_id\",\"session_id\":\"test_session_id\",\"instance_id\":\"test_instance_id\",\"integration_name\":\"test_integration_name\",\"integration_type\":\"test_integration_type\",\"integration_version\":\"test_integration_version\",\"lib_version\":\"test_library_version\",\"components\":[]}")
	}
}
