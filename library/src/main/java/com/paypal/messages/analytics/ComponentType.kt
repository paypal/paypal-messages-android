package com.paypal.messages.analytics

import com.google.gson.annotations.SerializedName

enum class ComponentType {
	@SerializedName("modal")
	MODAL,

	@SerializedName("message")
	MESSAGE,

	@SerializedName("standalone_modal")
	STANDALONE_MODAL,
	;

	override fun toString(): String {
		return super.toString().lowercase()
	}
}
