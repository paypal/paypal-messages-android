package com.paypal.messages.io

import com.google.gson.Gson
import com.paypal.messages.BuildConfig
import com.paypal.messages.errors.FailedToFetchDataException
import com.paypal.messages.logger.TrackingPayload
import com.paypal.messages.utils.LogCat
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.UUID
import com.paypal.messages.config.PayPalEnvironment as Env
import com.paypal.messages.config.PayPalMessageOfferType as OfferType
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig

object Api {
	private const val TAG = "Api"
	private val client = OkHttpClient()
	private val gson = Gson()
	var environment = Env.SANDBOX
	var instanceId: UUID? = null
	var originatingInstanceId: UUID? = null
	var sessionId: UUID? = null

	object Endpoints {
		private val ROOT_URLS = mapOf(
			Env.LIVE to "https://www.paypal.com",
			Env.SANDBOX to "https://www.sandbox.paypal.com",
			Env.STAGE to BuildConfig.STAGE_URL,
			Env.STAGE_VPN to BuildConfig.STAGE_VPN_URL,
			Env.LOCAL to BuildConfig.LOCAL_URL,
		)

		private val rootUrl = ROOT_URLS[environment]
		private val presentmentUrl = if (environment === Env.LOCAL) "$rootUrl:8000" else "$rootUrl"
		private val loggerUrl = if (environment === Env.LOCAL) "$rootUrl:9090" else "$rootUrl"

		val messageData = "$presentmentUrl/credit-presentment/native/message".toHttpUrl()
		val messageHash = "$presentmentUrl/credit-presentment/merchant-profile".toHttpUrl()
		val modalData = "$presentmentUrl/credit-presentment/lander/modal".toHttpUrl()
		val logger = "$loggerUrl/track/native".toHttpUrl()
	}

	private fun HttpUrl.Builder.setMessageDataQuery(config: MessageConfig, hash: String?) {
		addQueryParameter("client_id", config.data?.clientId)
		addQueryParameter("devTouchpoint", "false")
		addQueryParameter("env", environment.name.lowercase())
		addQueryParameter("logo_type", config.style.logoType.name.lowercase())
		addQueryParameter("instance_id", instanceId.toString())
		addQueryParameter("session_id", sessionId.toString())

		config.data?.amount?.let { addQueryParameter("amount", it.toString()) }
		config.data?.buyerCountry?.let { addQueryParameter("buyer_country", it) }
		config.data?.currencyCode?.let { addQueryParameter("currency", it.name) }
		config.data?.offerType?.let { addQueryParameter("offer", it.name) }

		hash?.let { addQueryParameter("merchant_config", it) }
	}

	private fun createMessageDataRequest(config: MessageConfig, hash: String?): Request {
		val request = Request.Builder().apply {
			header("Accept", "application/json")
			header("Authorization", Credentials.basic(config.data?.clientId ?: "", ""))
			header("x-requested-by", "native-upstream-messages")

			val urlBuilder = Endpoints.messageData.newBuilder()
			urlBuilder.setMessageDataQuery(config, hash)
			url(urlBuilder.build())
		}.build()

		LogCat.debug(TAG, "getMessageDataRequest: $request")
		return request
	}

	fun callMessageDataEndpoint(config: MessageConfig, hash: String?): ApiResult {
		val request = createMessageDataRequest(config, hash)
		try {
			val response = client.newCall(request).execute()
			val code = response.code
			val bodyJson = response.body?.string()
			response.close()

			if (code != 200) return ApiResult.Failure(FailedToFetchDataException())

			LogCat.debug(TAG, "callMessageDataEndpoint response: $bodyJson")
			val body = gson.fromJson(bodyJson, ApiMessageData.Response::class.java)

			val isValidResponse = body?.content != null && body.meta != null
			return if (isValidResponse) {
				originatingInstanceId = body.meta?.originatingInstanceId
				ApiResult.Success(body)
			}
			else {
				ApiResult.getFailureWithDebugId(response.headers)
			}
		}
		catch (exception: IOException) {
			// Failed to fetch the data and there is no debugId
			return ApiResult.Failure(FailedToFetchDataException())
		}
	}

	private fun createMessageHashRequest(clientId: String): Request {
		val request = Request.Builder().apply {
			header("Accept", "application/json")
			header("Authorization", Credentials.basic(clientId, ""))
			header("x-requested-by", "native-upstream-messages")

			val urlBuilder = Endpoints.messageHash.newBuilder()
			urlBuilder.addQueryParameter("client_id", clientId)
			url(urlBuilder.build())
		}.build()

		LogCat.debug(TAG, "getMessageHashRequest: $request")
		return request
	}

	fun callMessageHashEndpoint(clientId: String): ApiResult {
		val request = createMessageHashRequest(clientId)
		try {
			val response = client.newCall(request).execute()
			val bodyJson = response.body?.string()
			val isFailedResponse = !response.isSuccessful
			val message = response.message
			response.close()

			if (isFailedResponse) {
				LogCat.error(TAG, "callMessageHashEndpoint error: ${message}\n$bodyJson")
				return ApiResult.getFailureWithDebugId(response.headers)
			}

			LogCat.debug(TAG, "callMessageHashEndpoint response: $bodyJson")

			val hashResponse = gson.fromJson(bodyJson, ApiHashData.Response::class.java)
			return ApiResult.Success(hashResponse)
		}
		catch (error: java.io.IOException) {
			return ApiResult.Failure(FailedToFetchDataException())
		}
	}

	fun createModalUrl(
		clientId: String,
		amount: Double?,
		buyerCountry: String?,
		offer: OfferType?,
	): String {
		val url = Endpoints.modalData.newBuilder().apply {
			addQueryParameter("client_id", clientId)
			addQueryParameter("integration_type", BuildConfig.INTEGRATION_TYPE)
			addQueryParameter("features", "native-modal")
			amount?.let { addQueryParameter("amount", amount.toString()) }
			buyerCountry?.let { addQueryParameter("buyer_country", buyerCountry) }
			offer?.let { addQueryParameter("offer", it.name) }
		}.build().toString()

		LogCat.debug(TAG, "createModalUrl: $url")
		return url
	}

	private fun createLoggerRequest(json: String): Request {
		val request = Request.Builder().apply {
			url(Endpoints.logger)
			post(json.toRequestBody("application/json".toMediaType()))
		}.build()

		LogCat.debug(TAG, "createLoggerRequest: $request\npayloadJson: $json")
		return request
	}

	fun callLoggerEndpoint(payload: TrackingPayload) {
		// TODO, Ensure __shared__ property is correctly converted and added to json payload
		val json = gson.toJson(payload)
		val request = createLoggerRequest(json)
		val response = client.newCall(request).execute()
		response.body?.string()?.let { LogCat.debug(TAG, "callLoggerEndpoint response: $it") }
	}
}
