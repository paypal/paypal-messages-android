package com.paypal.messages.io

import android.content.Context
import com.google.gson.Gson
import com.paypal.messages.BuildConfig
import com.paypal.messages.logger.CloudEvent
import com.paypal.messages.logger.TrackingPayload
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
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
	var env = Env.SANDBOX
	var devTouchpoint: Boolean = false
	var ignoreCache: Boolean = false
	var stageTag: String? = null
	var instanceId: UUID? = null
	var originatingInstanceId: UUID? = null
	var sessionId: UUID? = null
	var ioDispatcher = Dispatchers.IO

	private fun HttpUrl.Builder.setMessageDataQuery(config: MessageConfig, hash: String?) {
		addQueryParameter("client_id", config.data.clientID)
		addQueryParameter("devTouchpoint", devTouchpoint.toString())
		addQueryParameter("ignore_cache", ignoreCache.toString())
		addQueryParameter("instance_id", instanceId.toString())
		addQueryParameter("session_id", sessionId.toString())

		if (!stageTag.isNullOrBlank()) { addQueryParameter("stage_tag", stageTag) }
		config.style.logoType?.let { addQueryParameter("logo_type", it.name.lowercase()) }
		config.data.amount?.let { addQueryParameter("amount", it.toString()) }
		config.data.buyerCountry?.let { addQueryParameter("buyer_country", it) }
		config.data.offerType?.let { addQueryParameter("offer", it.name) }

		hash?.let { addQueryParameter("merchant_config", it) }
	}

	internal fun createMessageDataRequest(config: MessageConfig, hash: String?): Request {
		val request = Request.Builder().apply {
			header("Accept", "application/json")
			header("x-requested-by", "native-upstream-messages")

			val urlBuilder = env.url(Env.Endpoints.MESSAGE_DATA).newBuilder()
			urlBuilder.setMessageDataQuery(config, hash)
			url(urlBuilder.build())
		}.build()

		val query = request.url.query?.replace("&", "\n  ")
		LogCat.debug(TAG, "getMessageDataRequest:\n  $request\n  $query")
		return request
	}

	internal fun callMessageDataEndpoint(config: MessageConfig, hash: String?): ApiResult {
		LogCat.debug(TAG, "callMessageDataEndpoint hash: $hash")
		val request = createMessageDataRequest(config, hash)
		try {
			val response = client.newCall(request).execute()
			val code = response.code
			val bodyJson = response.body?.string()
			response.close()

			if (code != 200) return ApiResult.Failure(PayPalErrors.FailedToFetchDataException("Code was $code"))

			val bodyJsonNoFdata = bodyJson?.replace(""""fdata":".*?"""".toRegex(), "")
			LogCat.debug(TAG, "callMessageDataEndpoint response: $bodyJsonNoFdata")
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
		catch (error: IOException) {
			// Failed to fetch the data and there is no debugId
			return ApiResult.Failure(
				PayPalErrors.FailedToFetchDataException("Message Data IOException: ${error.message}"),
			)
		}
	}

	fun getMessageWithHash(
		context: Context,
		messageConfig: MessageConfig,
		onCompleted: OnActionCompleted,
	) {
		CoroutineScope(ioDispatcher).launch {
			val localStorage = LocalStorage(context)
			val merchantHash: String? = localStorage.merchantHash
			val merchantHashState = localStorage.getMerchantHashState()
			LogCat.debug(TAG, merchantHashState.message)

			val newHash: String? = when (merchantHashState) {
				LocalStorage.State.NO_HASH -> getAndStoreNewHash(context, messageConfig)
				LocalStorage.State.HASH_OLDER_THAN_HARD_TTL -> getAndStoreNewHash(context, messageConfig)
				LocalStorage.State.HASH_BETWEEN_SOFT_AND_HARD_TTL -> {
					getAndStoreNewHash(context, messageConfig)
					merchantHash
				}
				LocalStorage.State.HASH_YOUNGER_THAN_SOFT_TTL -> merchantHash
				else -> null
			}

			val messageData = callMessageDataEndpoint(messageConfig, newHash)
			withContext(Dispatchers.Main) {
				onCompleted.onActionCompleted(messageData)
			}
		}
	}

	internal fun createMessageHashRequest(clientId: String): Request {
		val request = Request.Builder().apply {
			header("Accept", "application/json")
			header("x-requested-by", "native-upstream-messages")

			val urlBuilder = env.url(Env.Endpoints.MERCHANT_PROFILE).newBuilder()
			urlBuilder.addQueryParameter("client_id", clientId)
			url(urlBuilder.build())
		}.build()

		LogCat.debug(TAG, "getMessageHashRequest: $request")
		return request
	}

	internal fun callMessageHashEndpoint(clientId: String): ApiResult {
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
		catch (error: IOException) {
			return ApiResult.Failure(
				PayPalErrors.FailedToFetchDataException("Hash Data IOException: ${error.message}"),
			)
		}
	}

	private suspend fun getAndStoreNewHash(context: Context, messageConfig: MessageConfig): String? {
		val clientId = messageConfig.data.clientID
		val localStorage = LocalStorage(context)
		val result = withContext(ioDispatcher) {
			callMessageHashEndpoint(clientId)
		}

		var hash: String? = null
		if (result is ApiResult.Success<*>) {
			val data = result.response as ApiHashData.Response
			localStorage.merchantHashData = data
			hash = localStorage.merchantHash
		}

		LogCat.debug(TAG, "fetchNewHash hash: $hash")
		return hash
	}

	fun createModalUrl(
		clientId: String,
		amount: Double?,
		buyerCountry: String?,
		offer: OfferType?,
	): String {
		val url = env.url(Env.Endpoints.MODAL_DATA).newBuilder().apply {
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

	internal fun createLoggerRequest(json: String): Request {
		val request = Request.Builder().apply {
			url(env.url(Env.Endpoints.LOGGER))
			post(json.toRequestBody("application/json".toMediaType()))
		}.build()

		val jsonNoFdata = json.replace(""""fdata":".*?"""".toRegex(), "")
		LogCat.debug(TAG, "createLoggerRequest: $request\npayloadJson: $jsonNoFdata")
		return request
	}

	fun callLoggerEndpoint(payload: TrackingPayload) {
		val json = gson.toJson(CloudEvent(data = payload))
		val request = createLoggerRequest(json)
		val response = client.newCall(request).execute()
		response.body?.string()?.let { LogCat.debug(TAG, "callLoggerEndpoint response: $it") }
	}
}
