package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class HashDataActionResponse(
	@SerializedName("hash")
	val hash: String?,
)
