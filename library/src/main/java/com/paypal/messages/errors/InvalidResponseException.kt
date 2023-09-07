package com.paypal.messages.errors

class InvalidResponseException(paypalDebugId: String): BaseException("Invalid Response", paypalDebugId)
