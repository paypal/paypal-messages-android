package com.paypal.messages.logger

import com.google.gson.annotations.SerializedName

data class TrackingEvent(
	@SerializedName("event_type")
	var eventType: EventType,
	@SerializedName("render_duration")
	var renderDuration: Int? = null,
	@SerializedName("request_duration")
	var requestDuration: Int? = null,
	@SerializedName("link_name")
	var linkName: String? = null,
	@SerializedName("link_src")
	var linkSrc: String? = null,
	@SerializedName("data")
	var data: String? = null,
	@SerializedName("error_name")
	var errorName: String? = null,
	@SerializedName("error_description")
	var errorDescription: String? = null,
)
