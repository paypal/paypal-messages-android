package com.paypal.messages.config.message

import com.paypal.messages.analytics.AnalyticsComponent
import com.paypal.messages.config.message.style.PayPalMessageAlignment as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

/**
 * [PayPalMessageStyle] holds data used to customize the style of a PayPalMessage component
 *
 * Note on textAlignment here vs [AnalyticsComponent].styleTextAlign:
 * To match with other integrations, the value here will be textAlignment
 * but for logging purposes, the [AnalyticsComponent] key will be style_text_align
 */
data class PayPalMessageStyle(
	val color: Color = Color.BLACK,
	val logoType: LogoType = LogoType.PRIMARY,
	val textAlignment: Align = Align.LEFT,
) : Cloneable {
	public override fun clone(): PayPalMessageStyle {
		return super.clone() as PayPalMessageStyle
	}
}
