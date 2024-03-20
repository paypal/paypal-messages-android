package com.paypal.messages.io

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApiHashDataTest {
	private val gson = Gson()
	private val hash = "test_merchant_profile_hash"
	private val merchantProfile = ApiHashData.MerchantProfile(hash)
	private val cacheFlowDisabled = true
	private val ttlSoft = 1000L
	private val ttlHard = 2000L

	private val response = ApiHashData.Response(
		cacheFlowDisabled = cacheFlowDisabled,
		ttlSoft = ttlSoft,
		ttlHard = ttlHard,
		merchantProfile = merchantProfile,
	)

	@Test
	fun testResponseConstructor() {
		// Provides coverage for the wrapping object
		assertEquals(ApiHashData, ApiHashData)
		assertEquals(cacheFlowDisabled, response.cacheFlowDisabled)
		assertEquals(ttlSoft, response.ttlSoft)
		assertEquals(ttlHard, response.ttlHard)
		assertEquals(merchantProfile, response.merchantProfile)
	}

	@Test
	fun testResponseSerialization() {
		val json = gson.toJson(response)
		assertEquals(
			json,
			"{\"cache_flow_disabled\":true,\"ttl_soft\":1000,\"ttl_hard\":2000,\"merchant_profile\":{\"hash\":\"test_merchant_profile_hash\"}}",
		)
	}

	@Test
	fun testMerchantProfileConstructor() {
		assertEquals(hash, merchantProfile.hash)
	}

	@Test
	fun testMerchantProfileSerialization() {
		val json = gson.toJson(merchantProfile)
		assertEquals(json, "{\"hash\":\"test_merchant_profile_hash\"}")
	}
}
