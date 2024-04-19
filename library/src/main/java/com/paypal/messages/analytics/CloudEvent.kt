package com.paypal.messages.analytics

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

data class CloudEvent(
	@SerializedName("specversion")
	val specVersion: String = "1.0",
	val id: UUID = UUID.randomUUID(),
	val type: String = "com.paypal.credit.upstream-presentment.v1",
	val source: String = "urn:paypal:event-src:v1:android:messages",
	@SerializedName("datacontenttype")
	val dataContentType: String = "application/json",
	@Suppress("ktlint:standard:max-line-length")
	@SerializedName("dataschema")
	val dataSchema: String = "ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json",
	var time: String = "",
	val data: AnalyticsPayload,
) {
	init {
		val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		val currentDate = Date()

		val formattedDate = dateFormat.format(currentDate)
		time = formattedDate
	}
}
