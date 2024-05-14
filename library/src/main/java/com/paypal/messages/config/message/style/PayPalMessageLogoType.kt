package com.paypal.messages.config.message.style

import com.google.gson.annotations.SerializedName
import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessageLogoType] provides different variations of LogoTypes supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a LogoType
 */
enum class PayPalMessageLogoType(val value: Int) {
	@SerializedName("primary")
	PRIMARY(0),

	@SerializedName("alternative")
	ALTERNATIVE(1),

	@SerializedName("inline")
	INLINE(2),

	@SerializedName("none")
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
