package com.paypal.messages.config.modal

import com.google.gson.annotations.SerializedName

data class ModalCloseButton(
	@SerializedName("width")
	var width: Int? = null,
	@SerializedName("height")
	var height: Int? = null,
	@SerializedName("available_width")
	var availableWidth: Int? = null,
	@SerializedName("available_height")
	var availableHeight: Int? = null,
	@SerializedName("color")
	var color: String? = null,
	@SerializedName("color_type")
	var colorType: String? = null,
	@SerializedName("alternative_text")
	var alternativeText: String? = null,
) {
	init {
		width = width ?: 26
		height = height ?: 26
		availableWidth = availableWidth ?: 60
		availableHeight = availableHeight ?: 60
		color = color ?: "#001435"
		colorType = colorType ?: "dark"
		alternativeText = alternativeText ?: "PayPal learn more modal close"
	}
}
