package com.paypal.messages.errors

open class BaseException(message: String, var paypalDebugId: String?) :
	Exception("PayPalMessageException: $message")
