package com.paypal.messages.config.message

import com.paypal.messages.config.message.style.PayPalMessageAlign as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

/**
 * [PayPalMessageStyle] holds data used to customize the style of a PayPalMessage component
 */
data class PayPalMessageStyle(
	val color: Color = Color.BLACK,
	val logoType: LogoType = LogoType.PRIMARY,
	val textAlign: Align = Align.LEFT,
)
