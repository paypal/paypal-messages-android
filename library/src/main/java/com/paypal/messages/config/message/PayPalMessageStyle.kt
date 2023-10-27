package com.paypal.messages.config.message

import com.paypal.messages.config.message.style.PayPalMessageAlign as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

/**
 * [PayPalMessageStyle] holds data used to customize the style of a PayPalMessage component
 */
data class PayPalMessageStyle(
	val color: Color? = null,
	val logoType: LogoType? = null,
	val textAlign: Align? = null,
) {
	fun merge(newStyle: PayPalMessageStyle): PayPalMessageStyle {
		return this.copy(
			color = newStyle.color ?: this.color,
			logoType = newStyle.logoType ?: this.logoType,
			textAlign = newStyle.textAlign ?: this.textAlign,
		)
	}
}
