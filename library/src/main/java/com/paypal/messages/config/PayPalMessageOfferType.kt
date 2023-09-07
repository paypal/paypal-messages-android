package com.paypal.messages.config

import com.paypal.messages.utils.createFormattedIllegalArgumentException

/**
 * [PayPalMessageOfferType] provides different variations of OfferTypes supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a OfferType
 */
enum class PayPalMessageOfferType(val value: Int) {
	PAY_LATER_SHORT_TERM(0),
	PAY_LATER_LONG_TERM(1),
	PAY_LATER_PAY_IN_1(2),
	PAYPAL_CREDIT_NO_INTEREST(3);

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessageOfferType].
		 * If an invalid [attributeIndex] is provided then it will throw an [IllegalArgumentException].
		 *
		 * @throws [IllegalArgumentException] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessageOfferType {
			return when (attributeIndex) {
				PAY_LATER_SHORT_TERM.value -> PAY_LATER_SHORT_TERM
				PAY_LATER_LONG_TERM.value -> PAY_LATER_LONG_TERM
				PAY_LATER_PAY_IN_1.value -> PAY_LATER_PAY_IN_1
				PAYPAL_CREDIT_NO_INTEREST.value -> PAYPAL_CREDIT_NO_INTEREST
				else -> throw createFormattedIllegalArgumentException("OfferType", 3)
			}
		}
	}
}
