package com.paypal.messages.config

/**
 * Enum representing supported PayPal languages
 * @property code The language code (e.g., "en-US", "es-ES", "fr-FR")
 */
enum class PayPalLanguage(val code: String) {
	US_ENGLISH("en-US"),
	GB_ENGLISH("en-GB"),
	AUSTRALIA_ENGLISH("en-AU"),
	CANADA_ENGLISH("en-CA"),
	CANADA_FRENCH("fr-CA"),
	SPAIN("es-ES"),
	FRANCE("fr-FR"),
	GERMANY("de-DE"),
	AUSTRIA("de-AT"),
	ITALY("it-IT"),
	;

	companion object {
		/**
		 * Get PayPalLanguage from a string code. Returns null if not found.
		 */
		fun fromCode(code: String?): PayPalLanguage? {
			return values().find { it.code.equals(code, ignoreCase = true) }
		}
	}
}
