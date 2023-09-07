package com.paypal.messages.config.message

import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageLogoType

/**
 * [PayPalMessageStyle] holds data used to customize the style of a PayPalMessage component
 */
data class PayPalMessageStyle(
	val logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY,
	val color: PayPalMessageColor = PayPalMessageColor.BLACK,
	val textAlign: PayPalMessageAlign = PayPalMessageAlign.LEFT,
)
