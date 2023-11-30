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
	val softTtl: Long
		get() = merchantHashData?.ttlSoft ?: 0
	val hardTtl: Long
		get() = merchantHashData?.ttlHard ?: 0
	private var timestamp: Long
		get() = sharedPrefs.getLong("${StorageKeys.TIMESTAMP}", currentTimestamp)
		set(timestampArg) {
			val editor = sharedPrefs.edit()
			editor.putLong("${StorageKeys.TIMESTAMP}", timestampArg)
			editor.apply()
		}
	private val ageOfMerchantHash: Long
		get() = (System.currentTimeMillis() / 1000) - timestamp

	enum class State(val message: String) {
		NO_HASH("No hash in local storage. Fetching new hash"),
		HASH_OLDER_THAN_HARD_TTL("Local hash is older than hardTtl. Fetching new hash."),
		HASH_BETWEEN_SOFT_AND_HARD_TTL(
			"Local hash is older than softTtl. Using local hash. Storing new hash."
		),
		HASH_YOUNGER_THAN_SOFT_TTL("Local hash is younger than softTtl. Using local hash."),
		CACHE_DISABLED("Cache disabled. Omitting hash."),
	}

	fun getMerchantHashState(): State {
		return if (merchantHash === null) {
			State.NO_HASH
		}
		else if (!isCacheFlowDisabled!!) {
			when (ageOfMerchantHash) {
				in hardTtl..Long.MAX_VALUE -> State.HASH_OLDER_THAN_HARD_TTL
				in softTtl..hardTtl -> State.HASH_BETWEEN_SOFT_AND_HARD_TTL
				else -> State.HASH_YOUNGER_THAN_SOFT_TTL
			}
		}
		else {
			State.CACHE_DISABLED
		}
	}
}
