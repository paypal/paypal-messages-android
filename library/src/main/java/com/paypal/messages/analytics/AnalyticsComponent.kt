package com.paypal.messages.analytics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.message.PayPalMessageStyle
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.extensions.getJsonObject
import com.paypal.messages.utils.KoverExcludeGenerated

/**
 * [AnalyticsComponent] holds data used for logging analytics information
 *
 * Note on styleTextAlign here vs [PayPalMessageStyle].textAlignment:
 * To match with other integrations, the [PayPalMessageStyle] value will be textAlignment
 * but for logging purposes, the key will be style_text_align
 */
data class AnalyticsComponent(
	// Integration Details
	@SerializedName("offer_type")
	val offerType: PayPalMessageOfferType? = null,
	@SerializedName("amount")
	val amount: String? = null,
	@SerializedName("page_type")
	val pageType: PayPalMessagePageType? = null,
	@SerializedName("buyer_country_code")
	val buyerCountryCode: String? = null,
	@SerializedName("presentment_channel")
	val channel: String? = "UPSTREAM",

	// Message Only
	@SerializedName("style_logo_type")
	val styleLogoType: PayPalMessageLogoType? = null,
	@SerializedName("style_color")
	val styleColor: PayPalMessageColor? = null,
	@SerializedName("style_text_align")
	val styleTextAlign: PayPalMessageAlignment? = null,
	@SerializedName("message_type")
	val messageType: String? = null,

	// Modal Only
	@SerializedName("views")
	val views: MutableList<String>? = null,
	@SerializedName("qualified_products")
	val qualifiedProducts: MutableList<String>? = null,
	@SerializedName("integration_identifier")
	val integrationIdentifier: IntegrationIdentifier? = null,

	// Tracking Payload
	@SerializedName("fdata")
	val fdata: String? = null,
	@SerializedName("debug_id")
	val debugId: String? = null,
	@SerializedName("experimentation_experience_ids")
	val experimentationExperienceIds: MutableList<String>? = null,
	@SerializedName("experimentation_treatment_ids")
	val experimentationTreatmentIds: MutableList<String>? = null,
	@SerializedName("credit_product_identifiers")
	val creditProductIdentifiers: MutableList<String>? = null,
	@SerializedName("offer_country_code")
	val offerCountryCode: String? = null,
	@SerializedName("merchant_country_code")
	val merchantCountryCode: String? = null,
	@SerializedName("type")
	val type: String? = null,
	@SerializedName("instance_id")
	val instanceId: String? = null,
	@SerializedName("originating_instance_id")
	val originatingInstanceId: String? = null,
	@SerializedName("component_events")
	val componentEvents: MutableList<AnalyticsEvent>,

	// Dynamic Properties, not serialized by default
	@Suppress("PropertyName")
	val __shared__: MutableMap<String, Any>? = mutableMapOf(),
) {
	// TODO: Move all Gson to its own wrapping class
	@KoverExcludeGenerated
	fun getData(): JsonObject {
		val gson: Gson = GsonBuilder().setPrettyPrinting().create()
		val componentJson = gson.getJsonObject(this)

		val sharedJson = componentJson.get("__shared__")
		if (sharedJson != null) {
			val sharedJsonCopy = sharedJson.deepCopy().asJsonObject
			componentJson.remove("__shared__")

			for ((key, value) in sharedJsonCopy.entrySet()) {
				componentJson.add(key, value)
			}
		}

		return componentJson
	}
}
