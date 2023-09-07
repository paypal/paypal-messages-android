package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class ActionResponseVariables(
	@SerializedName("inline_logo_placeholder")
	val logoPlaceholder: String?,
)
