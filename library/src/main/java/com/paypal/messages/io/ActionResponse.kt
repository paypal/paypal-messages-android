package com.paypal.messages.io

import com.google.gson.annotations.SerializedName

data class ActionResponse(
	@SerializedName("meta")
	val meta: ActionResponseMetadata?,
	@SerializedName("content")
	val content: ActionResponseContent?,
): ApiResult.ApiResponse()
