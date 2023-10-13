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
			val newHash: String?

			if (merchantHash === null) {
				LogCat.debug(TAG, "No hash in local storage. Fetching new hash")
				newHash = fetchNewHash(messageConfig)
			}
			else if (!localStorage.isCacheFlowDisabled!!) {
				val ageOfStoredHash = localStorage.ageOfMerchantHash
				val softTtl = localStorage.softTtl!!
				val hardTtl = localStorage.hardTtl!!

				newHash = when (ageOfStoredHash) {
					in hardTtl..Long.MAX_VALUE -> {
						LogCat.debug(TAG, "Local hash is older than hardTtl. Fetching new hash.")
						fetchNewHash(messageConfig)
					}
					in softTtl..hardTtl -> {
						LogCat.debug(
							TAG, "Local hash is older than softTtl. Using local hash. Storing new hash.",
						)
						fetchNewHash(messageConfig)
						merchantHash
					}
					else -> {
						LogCat.debug(TAG, "Local hash is younger than softTtl. Using local hash.")
						merchantHash
					}
				}
			}
			else {
				LogCat.debug(TAG, "Cache disabled. Omitting hash.")
				newHash = null
			}

			val messageData = Api.callMessageDataEndpoint(messageConfig, newHash)
			withContext(Dispatchers.Main) {
				onCompleted.onActionCompleted(messageData)
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
			val data = result.response as ApiHashData.Response
			localStorage.merchantHashData = data
			hash = localStorage.merchantHash
		}

		LogCat.debug(TAG, "fetchNewHash hash: $hash")
		return hash
	}
}
