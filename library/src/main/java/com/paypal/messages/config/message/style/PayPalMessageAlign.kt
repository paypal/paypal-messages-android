package com.paypal.messages.config.message.style

import com.paypal.messages.errors.IllegalEnumArg

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
		 * If an invalid [attributeIndex] is provided then it will throw an [IllegalArgumentException].
		 *
		 * @throws [IllegalArgumentException] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageAlign {
			return when (attributeIndex) {
				LEFT.value -> LEFT
				CENTER.value -> CENTER
				RIGHT.value -> RIGHT
				else -> throw IllegalEnumArg("LogoAlignment", 3)
			}
		}
	}
}
