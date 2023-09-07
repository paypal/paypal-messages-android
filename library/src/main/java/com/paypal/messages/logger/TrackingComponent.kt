package com.paypal.messages.logger

import com.google.gson.annotations.SerializedName
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.PayPalMessageOfferType

data class TrackingComponent(
	// Integration Details
	@SerializedName("offer_type")
	val offerType: PayPalMessageOfferType? = null,
	@SerializedName("amount")
	val amount: String? = null,
	@SerializedName("placement")
	val placement: String? = null,
	@SerializedName("buyer_country_code")
	val buyerCountryCode: String? = null,
	@SerializedName("channel")
	val channel: String? = "NATIVE",

	// Message Only
	@SerializedName("style_logo_type")
	val styleLogoType: PayPalMessageLogoType? = null,
	@SerializedName("style_color")
	val styleColor: PayPalMessageColor? = null,
	@SerializedName("style_text_align")
	val styleTextAlign: PayPalMessageAlign? = null,
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
	@SerializedName("events")
	val events: MutableList<TrackingEvent>,

	// Dynamic Properties, not serialized by default
	val __shared__: MutableMap<String, Any>? = mutableMapOf(),
)
