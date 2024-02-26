package com.paypal.messages.config

import com.paypal.messages.utils.PayPalErrors

/**
 * [PayPalMessagePageType] provides different variations of PageTypes supported by the PayPalMessage component
 *
 * @property value is the index identifier for returning a PageType
 */
enum class PayPalMessagePageType(val value: Int) {
	CART(0),
	CHECKOUT(1),
	HOME(2),
	MINI_CART(3),
	PRODUCT_DETAILS(4),
	PRODUCT_LISTING(5),
	SEARCH_RESULTS(6),
	;

	companion object {
		/**
		 * Given an [attributeIndex] this will provide the correct [PayPalMessagePageType].
		 *
		 * @throws [PayPalErrors.IllegalEnumArg] when an invalid index is provided.
		 */
		operator fun invoke(attributeIndex: Int): PayPalMessagePageType {
			return when (attributeIndex) {
				CART.value -> CART
				CHECKOUT.value -> CHECKOUT
				HOME.value -> HOME
				MINI_CART.value -> MINI_CART
				PRODUCT_DETAILS.value -> PRODUCT_DETAILS
				PRODUCT_LISTING.value -> PRODUCT_LISTING
				SEARCH_RESULTS.value -> SEARCH_RESULTS
				else -> throw PayPalErrors.IllegalEnumArg("PageType", 7)
			}
		}
	}
}
