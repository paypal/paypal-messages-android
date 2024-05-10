package com.paypal.messages.config.message.style

import com.google.gson.annotations.SerializedName
import com.paypal.messages.R
import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageColor] provides different variations of colors supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a color
 */
enum class PayPalMessageColor(
	val value: Int,
	val colorResId: Int,
) {
	@SerializedName("black")
	BLACK(value = 0, colorResId = R.color.gray_700),

	@SerializedName("white")
	WHITE(value = 1, colorResId = R.color.white),

	@SerializedName("monochrome")
	MONOCHROME(value = 2, colorResId = R.color.black),

	@SerializedName("grayscale")
	GRAYSCALE(value = 3, colorResId = R.color.gray_700),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageColor].
		 *
		 * @throws [PayPalErrors.IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageColor {
			return when (attributeIndex) {
				BLACK.value -> BLACK
				WHITE.value -> WHITE
				MONOCHROME.value -> MONOCHROME
				GRAYSCALE.value -> GRAYSCALE
				else -> throw PayPalErrors.IllegalEnumArg("Color", 4)
			}
		}
	}
}
