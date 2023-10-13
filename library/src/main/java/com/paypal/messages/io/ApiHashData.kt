package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

object ApiHashData {
	data class Response(
		@SerializedName("cache_flow_disabled")
		val cacheFlowDisabled: Boolean?,
		@SerializedName("ttl_soft")
		val ttlSoft: Long?,
		@SerializedName("ttl_hard")
		val ttlHard: Long?,
		@SerializedName("merchant_profile")
		val merchantProfile: MerchantProfile?,
	) : ApiResult.ApiResponse()

	data class MerchantProfile(
		@SerializedName("hash")
		val hash: String?,
	)
}
