package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class ActionResponseContent(
	@SerializedName("default")
	val default: ActionContent?,
	@SerializedName("generic")
	val generic: ActionContent?,
)
