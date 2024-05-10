package com.paypal.messages.config.message.style

import com.google.gson.annotations.SerializedName
import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageAlignment] provides different variations of text alignments supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a text alignment
 */
enum class PayPalMessageAlignment(val value: Int) {
	@SerializedName("left")
	LEFT(0),

	@SerializedName("center")
	CENTER(1),

	@SerializedName("right")
	RIGHT(2),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageAlignment].
		 *
		 * @throws [PayPalErrors.IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageAlignment {
			return when (attributeIndex) {
				LEFT.value -> LEFT
				CENTER.value -> CENTER
				RIGHT.value -> RIGHT
				else -> throw PayPalErrors.IllegalEnumArg("PayPalMessageAlignment", 3)
			}
		}
	}
}
