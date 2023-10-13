package com.paypal.messages.utils

object PayPalErrors {
	open class Base(message: String, val debugId: String? = null) :
		Exception("PayPalMessageException\n  debugId: $debugId\n  message: $message")

	class FailedToFetchDataException(message: String, debugId: String? = null) :
		Base("Failed to get Message Data: $message", debugId)

	class IllegalEnumArg(enumName: String, enumValues: Int) :
		Base(
			"Attempted to create a $enumName with an invalid index. " +
				"Please use an index that is between 0 and ${enumValues - 1} and try again."
		)

	class InvalidCheckoutConfigException(message: String, debugId: String? = null) :
		Base("Invalid Checkout Config: $message")

	class InvalidClientIdException(message: String, debugId: String? = null) :
		Base("Invalid ClientID: $message", debugId)

	class InvalidResponseException(message: String, debugId: String? = null) :
		Base("Invalid Response: $message", debugId)

	class ModalFailedToLoad(message: String, debugId: String? = null) :
		Base("Modal failed to open: $message", debugId)
}
