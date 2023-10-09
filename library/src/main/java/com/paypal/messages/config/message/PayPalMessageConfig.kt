package com.paypal.messages.config.message

import com.paypal.messages.logger.Logger

/**
 * [PayPalMessageConfig] is the main configuration model for interacting with the PayPalMessage component
 * @param data - [PayPalMessageData] model with fields that impact the message content to display
 * @param style - [PayPalMessageStyle] model with fields that impact the style of the message component
 * @param viewState - [PayPalMessageViewState] model with callbacks used to track the process of getting the message to display
 * @param events - [PayPalMessageEvents] model with callbacks used to track the interaction with the message component
 */
data class PayPalMessageConfig(
	var data: PayPalMessageData? = null,
	var style: PayPalMessageStyle = PayPalMessageStyle(),
	var viewState: PayPalMessageViewState? = null,
	var events: PayPalMessageEvents? = null,
) {
	fun setGlobalAnalytics(
		integrationName: String,
		integrationVersion: String,
	) {
		Logger.getInstance(data?.clientId ?: "").setGlobalAnalytics(integrationName, integrationVersion)
	}
}
