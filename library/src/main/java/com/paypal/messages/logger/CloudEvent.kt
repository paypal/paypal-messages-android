package com.paypal.messages.logger
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date


data class CloudEvent(
    val specversion: String = "1.0",
	val id: UUID = UUID.randomUUID(),
    val type: String = "com.paypal.credit.upstream-presentment.v1",
    val source: String = "urn:paypal:event-src:v1:android:messages",
    val datacontenttype : String = "application/json",
    val dataschema: String = "ppaas:events.credit.FinancingPresentmentAsyncAPISpecification/v1/schema/json/credit_upstream_presentment_event.json",
	var time: String = "",
    val data: TrackingPayload,
){
    init {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val currentDate = Date()

        val formattedDate = dateFormat.format(currentDate)
        time = formattedDate
    }
}
