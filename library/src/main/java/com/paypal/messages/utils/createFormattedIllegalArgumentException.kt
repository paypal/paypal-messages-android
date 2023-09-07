package com.paypal.messages.utils

internal fun createFormattedIllegalArgumentException(
	enumName: String,
	enumValues: Int,
): IllegalArgumentException {
	val exceptionMessage = "PayPalMessage Attempted to create a $enumName with an invalid index. " +
		"Please use an index that is between 0 and ${enumValues - 1} and try again."
	return IllegalArgumentException(exceptionMessage)
}
