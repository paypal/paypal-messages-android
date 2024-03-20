package com.paypal.messages.config.message

import com.paypal.messages.logger.Logger

/**
 * [PayPalMessageConfig] is the main configuration model for interacting with the PayPalMessage component
 * @param data - [PayPalMessageData] model with fields that impact the message content to display
 * @param style - [PayPalMessageStyle] model with fields that impact the style of the message component
 * @param viewStateCallbacks - [PayPalMessageViewStateCallbacks] model with callbacks used to track the process of getting the message to display
 * @param eventsCallbacks - [PayPalMessageEventsCallbacks] model with callbacks used to track the interaction with the message component
 */
data class PayPalMessageConfig(
	var data: PayPalMessageData,
	var style: PayPalMessageStyle = PayPalMessageStyle(),
	var viewStateCallbacks: PayPalMessageViewStateCallbacks? = null,
	var eventsCallbacks: PayPalMessageEventsCallbacks? = null,
) : Cloneable {
	public override fun clone() = PayPalMessageConfig(data.clone(), style, viewStateCallbacks?.clone(), eventsCallbacks?.clone())

	fun setGlobalAnalytics(
		integrationName: String = "",
		integrationVersion: String = "",
		deviceId: String = "",
		sessionId: String = "",
	) {
		if (data.clientID != "") {
			Logger.getInstance(data.clientID).setGlobalAnalytics(
				integrationName,
				integrationVersion,
				deviceId,
				sessionId,
			)
		}
	}
}
