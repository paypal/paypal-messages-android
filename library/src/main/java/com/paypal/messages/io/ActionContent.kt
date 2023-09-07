package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class ActionContent(
	@SerializedName("main")
	val main: String?,
	@SerializedName("disclaimer")
	val disclaimer: String?,
)
