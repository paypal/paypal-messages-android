package com.paypal.messages.config.message.style

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageLogoType] provides different variations of LogoTypes supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a LogoType
 */
enum class PayPalMessageLogoType(val value: Int) {
	PRIMARY(0),
	ALTERNATIVE(1),
	INLINE(2),
	NONE(3),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageLogoType].
		 *
		 * @throws [IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageLogoType {
			return when (attributeIndex) {
				PRIMARY.value -> PRIMARY
				ALTERNATIVE.value -> ALTERNATIVE
				INLINE.value -> INLINE
				NONE.value -> NONE
				else -> throw PayPalErrors.IllegalEnumArg("LogoType", 4)
			}
		}
	}
}
