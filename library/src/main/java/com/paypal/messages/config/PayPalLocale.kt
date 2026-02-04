package com.paypal.messages.config

/**
 * Enum representing supported PayPal locales
 * @property code The locale code (e.g., "en_US", "es_ES", "fr_FR")
 */
enum class PayPalLocale(val code: String) {
	US_ENGLISH("en_US"),
	GB_ENGLISH("en_GB"),
	AUSTRALIA_ENGLISH("en_AU"),
	CANADA_ENGLISH("en_CA"),
	CANADA_FRENCH("fr_CA"),
	SPAIN("es_ES"),
	FRANCE("fr_FR"),
	GERMANY("de_DE"),
	AUSTRIA("de_AT"),
	ITALY("it_IT"),
	;

	companion object {
		/**
		 * Get PayPalLocale from a string code. Returns null if not found.
		 */
		fun fromCode(code: String?): PayPalLocale? {
			return values().find { it.code.equals(code, ignoreCase = true) }
		}
	}
}
