package com.paypal.messages.analytics

import com.google.gson.annotations.SerializedName

data class AnalyticsEvent(
	@SerializedName("event_type")
	var eventType: EventType,
	@SerializedName("render_duration")
	var renderDuration: String? = null,
	@SerializedName("request_duration")
	var requestDuration: String? = null,
	@SerializedName("page_view_link_name")
	var pageViewLinkName: String? = null,
	@SerializedName("page_view_link_source")
	var pageViewLinkSource: String? = null,
	@SerializedName("data")
	var data: String? = null,
	@SerializedName("error_name")
	var errorName: String? = null,
	@SerializedName("error_description")
	var errorDescription: String? = null,
)
