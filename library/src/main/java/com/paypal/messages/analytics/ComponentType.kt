package com.paypal.messages.analytics

enum class ComponentType {
	MODAL,
	MESSAGE,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
