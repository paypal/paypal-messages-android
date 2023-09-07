package com.paypal.messages.errors

class ModalFailedToLoad(message: String): BaseException("Modal failed to open: $message", null)
