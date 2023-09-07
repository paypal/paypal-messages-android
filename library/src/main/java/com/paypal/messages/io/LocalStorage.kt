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

	enum class StorageKeys(private val keyName: String) {
		MERCHANT_HASH_DATA("merchantProfileHashData"),
		TIMESTAMP("timestamp");
		override fun toString(): String { return keyName }
	}

	var merchantHashData: HashActionResponse?
		get() {
			val storage = sharedPrefs.getString("${StorageKeys.MERCHANT_HASH_DATA}", null)
			LogCat.debug(TAG, "Retrieving hash from local storage:\n$storage")
			if (storage !== null) {
				val storageFromString = gson.fromJson(storage, HashActionResponse::class.java)
				LogCat.debug(TAG, "Converted local storage string to json:\n${storageFromString}")
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
		}

	val isCacheFlowDisabled: Boolean?
		get() = merchantHashData?.cacheFlowDisabled
	val merchantHash: String?
		get() = merchantHashData?.merchantProfile?.hash
	val softTtl: Long?
		get() = merchantHashData?.ttlSoft
	val hardTtl: Long?
		get() = merchantHashData?.ttlHard
	var timestamp: Long
		get() = sharedPrefs.getLong("${StorageKeys.TIMESTAMP}", currentTimestamp)
		set(timestampArg) {
			val editor = sharedPrefs.edit()
			editor.putLong("${StorageKeys.TIMESTAMP}", timestampArg)
			editor.apply()
		}
	val ageOfMerchantHash: Long
		get() = (System.currentTimeMillis() / 1000) - timestamp
}
