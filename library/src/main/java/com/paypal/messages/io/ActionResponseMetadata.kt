package com.paypal.messages.io

import com.google.gson.annotations.SerializedName
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.modal.ModalCloseButton
import java.util.UUID

data class ActionResponseMetadata(
	@SerializedName("credit_product_group")
	val creditProductGroup: ProductGroup?,
	@SerializedName("offer_country_code")
	val offerCountryCode: String?,
	@SerializedName("offer_type")
	val offerType: PayPalMessageOfferType?,
	@SerializedName("message_type")
	val messageType: String?,
	@SerializedName("modal_close_button")
	val modalCloseButton: ModalCloseButton,
	@SerializedName("variables")
	val variables: ActionResponseVariables?,
	@SerializedName("merchant_country_code")
	val merchantCountryCode: String,
	@SerializedName("credit_product_identifiers")
	val creditProductIdentifiers: List<String>,
	@SerializedName("debug_id")
	val debug_id: String,
	@SerializedName("fdata")
	val fdata: String,
	@SerializedName("tracking_keys")
	val trackingKeys: List<String>,
	@SerializedName("originating_instance_id")
	val originatingInstanceId: UUID,
)
