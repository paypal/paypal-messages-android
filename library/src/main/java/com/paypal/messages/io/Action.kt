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
			val isCacheEnabled = !localStorage.isCacheFlowDisabled!!
			val ageOfStoredHash = localStorage.ageOfMerchantHash
			val softTtl = localStorage.softTtl!!
			val hardTtl = localStorage.hardTtl!!
			val newHash: String?

			if (merchantHash === null) {
				LogCat.debug(TAG, "No hash in local storage. Fetching new hash")
				newHash = Api.getAndStoreNewHash(context, messageConfig)
			}
			else if (isCacheEnabled) {
				val message = "Local hash is " + when (ageOfStoredHash) {
					in hardTtl..Long.MAX_VALUE -> "older than hardTtl. Fetching new hash."
					in softTtl..hardTtl -> "older than softTtl. Using local hash. Storing new hash."
					else -> "younger than softTtl. Using local hash."
				}
				LogCat.debug(TAG, message)

				if (ageOfStoredHash in softTtl..hardTtl) Api.getAndStoreNewHash(context, messageConfig)
				newHash = if (ageOfStoredHash > hardTtl) Api.getAndStoreNewHash(context, messageConfig) else merchantHash
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
}
