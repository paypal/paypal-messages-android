package com.paypal.messages.config.modal

import com.google.gson.annotations.SerializedName

data class ModalCloseButton(
	@SerializedName("width")
	val width: Int,
	@SerializedName("height")
	val height: Int,
	@SerializedName("available_width")
	val availableWidth: Int,
	@SerializedName("available_height")
	val availableHeight: Int,
	@SerializedName("color")
	val color: String,
	@SerializedName("color_type")
	val colorType: String,
	@SerializedName("alternative_text")
	val alternativeText: String,
)
