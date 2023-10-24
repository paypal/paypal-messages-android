package com.paypal.messages.config.message.style

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageAlign] provides different variations of text alignments supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a text alignment
 */
enum class PayPalMessageAlign(val value: Int) {
	LEFT(0),
	CENTER(1),
	RIGHT(2),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageAlign].
		 *
		 * @throws [PayPalErrors.IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageAlign {
			return when (attributeIndex) {
				LEFT.value -> LEFT
				CENTER.value -> CENTER
				RIGHT.value -> RIGHT
				else -> throw PayPalErrors.IllegalEnumArg("LogoAlignment", 3)
			}
		}
	}
}
