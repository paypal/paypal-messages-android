package com.paypal.messages.analytics

import com.google.gson.annotations.SerializedName

enum class ComponentType {
	@SerializedName("modal")
	MODAL,

	@SerializedName("message")
	MESSAGE,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
