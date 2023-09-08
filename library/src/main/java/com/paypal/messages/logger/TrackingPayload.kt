package com.paypal.messages.logger

import com.google.gson.annotations.SerializedName
import com.paypal.messages.BuildConfig
import kotlin.random.Random

fun generateRandomInstance(length: Int = 10) : String {
	val charPool: List<Char> = ('a'..'z') + ('A' .. 'Z') + ( '0'..'9') + '-';
	return (1..length)
		.map { Random.nextInt(0, charPool.size) }
		.map(charPool::get)
		.joinToString("")
}

data class TrackingPayload(
	// Integration Details
	@SerializedName("client_id")
	val clientId: String,
	@SerializedName("merchant_id")
	val merchantId: String? = null,
	@SerializedName("partner_attribution_id")
	val partnerAttributionId: String? = null,
	@SerializedName("merchant_profile_hash")
	val merchantProfileHash: String? = null,

	// Global Details
	@SerializedName("device_id")
	val deviceId: String,
	@SerializedName("session_id")
	val sessionId: String,
	@SerializedName("instance_id")
	val instanceID: String = generateRandomInstance(),
	@SerializedName("integration_name")
	val integrationName: String,
	@SerializedName("integration_type")
	val integrationType: String = BuildConfig.INTEGRATION_TYPE,
	@SerializedName("integration_version")
	val integrationVersion: String,
	@SerializedName("lib_version")
	val libraryVersion: String = BuildConfig.LIBRARY_VERSION,
	// Event Groups
	@SerializedName("components")
	val components: MutableList<TrackingComponent>,
)
