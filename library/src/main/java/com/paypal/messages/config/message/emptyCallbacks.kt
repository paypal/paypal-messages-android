package com.paypal.messages.config.message

import com.paypal.messages.utils.PayPalErrors

val empty = {}
val emptyError: (error: PayPalErrors.Base) -> Unit = {}
