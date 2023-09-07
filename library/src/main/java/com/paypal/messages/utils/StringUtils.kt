package com.paypal.messages.utils

/**
 *  Extension that returns the current string if it's not null nor empty, or  null otherwise.
 *  Useful for taking advantage of the elvis ?: operator in Kotlin.
 *  For example:
 *   val id = example.id.nullIfNullOrEmpty() ?: return
 *  @return the original string or @null if it's null or empty.
 */
fun nullIfNullOrEmpty(string: String): String? {
	return if (string.isNullOrEmpty()) {
		null
	}
	else {
		string
	}
}
