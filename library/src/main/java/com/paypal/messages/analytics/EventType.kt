package com.paypal.messages.analytics

enum class EventType {
	MESSAGE_RENDERED,
	MESSAGE_CLICKED,
	MODAL_RENDERED,
	MODAL_CLICKED,
	MODAL_OPENED,
	MODAL_CLOSED,
	MODAL_ERROR,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
