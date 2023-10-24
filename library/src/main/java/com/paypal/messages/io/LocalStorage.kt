package com.paypal.messages.io

import android.content.Context
import com.google.gson.Gson
import com.paypal.messages.utils.LogCat

class LocalStorage constructor(context: Context) {
	private val TAG = this::class.java.simpleName
	private val key = "PayPalUpstreamLocalStorage"
	private val sharedPrefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
	private val currentTimestamp = System.currentTimeMillis() / 1000
	private val gson = Gson()
	private var isFirstRetrieval = true

	enum class StorageKeys(private val keyName: String) {
		MERCHANT_HASH_DATA("merchantProfileHashData"),
		TIMESTAMP("timestamp"),
		;

		override fun toString(): String {
			return keyName
		}
	}

	var merchantHashData: ApiHashData.Response?
		get() {
			val storage = sharedPrefs.getString("${StorageKeys.MERCHANT_HASH_DATA}", null)
			if (storage !== null) {
				val storageFromString = gson.fromJson(storage, ApiHashData.Response::class.java)
				if (isFirstRetrieval) {
					LogCat.debug(
						TAG,
						"Converted storage string:\n" +
							"  storage string: $storage\n" +
							"  Api Hash Data : $storageFromString",
					)
					isFirstRetrieval = false
				}
				return storageFromString
			}
			return null
		}
		set(hashDataArg) {
			val editor = sharedPrefs.edit()
			LogCat.debug(TAG, "merchantHashData set: ${hashDataArg?.toJson()}")
			editor.putString("${StorageKeys.MERCHANT_HASH_DATA}", hashDataArg?.toJson())
			editor.apply()
			timestamp = currentTimestamp
			isFirstRetrieval = true
		}

	val isCacheFlowDisabled: Boolean?
		get() = merchantHashData?.cacheFlowDisabled
	val merchantHash: String?
		get() = merchantHashData?.merchantProfile?.hash
	val softTtl: Long?
		get() = merchantHashData?.ttlSoft
	val hardTtl: Long?
		get() = merchantHashData?.ttlHard
	private var timestamp: Long
		get() = sharedPrefs.getLong("${StorageKeys.TIMESTAMP}", currentTimestamp)
		set(timestampArg) {
			val editor = sharedPrefs.edit()
			editor.putLong("${StorageKeys.TIMESTAMP}", timestampArg)
			editor.apply()
		}
	val ageOfMerchantHash: Long
		get() = (System.currentTimeMillis() / 1000) - timestamp
}
