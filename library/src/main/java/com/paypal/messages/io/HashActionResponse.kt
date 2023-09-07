package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class HashActionResponse(
	@SerializedName("cache_flow_disabled")
	val cacheFlowDisabled: Boolean?,
	@SerializedName("ttl_soft")
	val ttlSoft: Long?,
	@SerializedName("ttl_hard")
	val ttlHard: Long?,
	@SerializedName("merchant_profile")
	val merchantProfile: HashDataActionResponse?,
): ApiResult.ApiResponse()
