package com.paypal.messages.config.message.style

import com.paypal.messages.R
import com.paypal.messages.errors.IllegalEnumArg

/**
 * [PayPalMessageColor] provides different variations of colors supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a color
 */
enum class PayPalMessageColor(
	val value: Int,
	val colorResId: Int,
) {
	BLACK(value = 0, colorResId = R.color.gray_700),
	WHITE(value = 1, colorResId = R.color.white),
	MONOCHROME(value = 2, colorResId = R.color.black),
	GRAYSCALE(value = 3, colorResId = R.color.gray_700),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageColor].
		 * If an invalid [attributeIndex] is provided then it will throw an [IllegalArgumentException].
		 *
		 * @throws [IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageColor {
			return when (attributeIndex) {
				BLACK.value -> BLACK
				WHITE.value -> WHITE
				MONOCHROME.value -> MONOCHROME
				GRAYSCALE.value -> GRAYSCALE
				else -> throw IllegalEnumArg("Color", 4)
			}
		}
	}
}
