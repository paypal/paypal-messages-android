package com.paypal.messages.analytics

import com.google.gson.annotations.SerializedName

enum class EventType {
	@SerializedName("message_rendered")
	MESSAGE_RENDERED,

	@SerializedName("message_clicked")
	MESSAGE_CLICKED,

	@SerializedName("modal_rendered")
	MODAL_RENDERED,

	@SerializedName("modal_clicked")
	MODAL_CLICKED,

	@SerializedName("modal_viewed")
	MODAL_VIEWED,

	@SerializedName("modal_closed")
	MODAL_CLOSED,

	@SerializedName("modal_error")
	MODAL_ERROR,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
