package com.paypal.messages.config

/**
 * Currency Code provides the set of currency codes supported by PayPal.
 *
 * @see [Currency Codes](https://developer.paypal.com/docs/api/reference/currency-codes/)
 */
enum class CurrencyCode {
	/**
	 * Currency Code for: Australian dollar
	 * Countries using this: AU (Australia)
	 * Symbol: $
	 */
	AUD,

	/**
	 * Currency Code for: Euro
	 * Countries using this: DE (Germany), ES (Spain), FR (France), IT (Italy)
	 * Symbol: €
	 */
	EUR,

	/**
	 * Currency Code for: British Pound
	 * Countries using this: GB (Great Britain / United Kingdom)
	 * Symbol: £
	 */
	GBP,

	/**
	 * Currency Code for: United States dollar
	 * Countries using this: US (United States)
	 * Symbol: $
	 */
	USD,
}
