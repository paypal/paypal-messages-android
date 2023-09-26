package com.paypal.messages.io

import android.content.Context
import com.paypal.messages.utils.LogCat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig

class Action(private val context: Context) {
	private val TAG = this::class.java.simpleName

	fun execute(messageConfig: MessageConfig, onCompleted: OnActionCompleted) {
		CoroutineScope(Dispatchers.IO).launch {
			val localStorage = LocalStorage(context)
			val merchantHash: String? = localStorage.merchantHash
			var newHash: String?

			if (merchantHash === null) {
				LogCat.debug(TAG, "Hash does not exist in local storage. Fetching new hash")
				newHash = fetchNewHash(messageConfig)

				val messageData = Api.callMessageDataEndpoint(messageConfig, newHash)
				withContext(Dispatchers.Main) {
					onCompleted.onActionCompleted(messageData)
				}
			}
			// If a previously stored hash is available AND cache_flow_disabled is false
			else if (!localStorage.isCacheFlowDisabled!!) {
				val ageOfStoredHash = localStorage.ageOfMerchantHash
				val softTtl = localStorage.softTtl!!
				val hardTtl = localStorage.hardTtl!!

				// If the stored hash is older than hard_ttl
				if (ageOfStoredHash < hardTtl) {
					// the stale value should be ignored and a new hash should be fetched
					LogCat.debug(TAG, "TTL expired. Fetching new hash.\n$merchantHash")
					newHash = fetchNewHash(messageConfig)

					val messageData = Api.callMessageDataEndpoint(messageConfig, newHash)
					withContext(Dispatchers.Main) {
						onCompleted.onActionCompleted(messageData)
					}
				} else if (ageOfStoredHash in softTtl..hardTtl) {
					LogCat.debug(TAG, "Fetching new hash but still using hash from local storage")
					val messageData = Api.callMessageDataEndpoint(messageConfig, merchantHash)
					withContext(Dispatchers.Main) {
						onCompleted.onActionCompleted(messageData)
					}
				} else {
					LogCat.debug(TAG, "Retrieving hash from local storage\n$merchantHash")
					val messageData = Api.callMessageDataEndpoint(messageConfig, merchantHash)
					withContext(Dispatchers.Main) {
						onCompleted.onActionCompleted(messageData)
					}
				}
			} else {
				LogCat.debug(TAG, "Omitting hash")
				val messageData = Api.callMessageDataEndpoint(messageConfig, null)
				withContext(Dispatchers.Main) {
					onCompleted.onActionCompleted(messageData)
				}
			}
		}
	}

	private suspend fun fetchNewHash(messageConfig: MessageConfig): String? {
		val clientId = messageConfig.data?.clientId ?: ""
		val localStorage = LocalStorage(context)
		val result = withContext(Dispatchers.IO) {
			Api.callMessageHashEndpoint(clientId)
		}

		var hash: String? = null
		if (result is ApiResult.Success<*>) {
			val data = result.response as HashActionResponse
			localStorage.merchantHashData = data
			hash = localStorage.merchantHash
		}

		LogCat.debug(TAG, "fetchNewHash hash: $hash")
		return hash
	}
}
