package com.paypal.messages.analytics

enum class EventType {
	MESSAGE_RENDER,
	MESSAGE_CLICK,
	MODAL_RENDER,
	MODAL_CLICK,
	MODAL_OPEN,
	MODAL_CLOSE,
	MODAL_ERROR,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
