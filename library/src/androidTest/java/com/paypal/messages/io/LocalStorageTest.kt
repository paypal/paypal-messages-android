package com.paypal.messages.io

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalStorageTest {
	private lateinit var context: Context
	private lateinit var sharedPreferences: SharedPreferences

	@Before
	fun setUp() {
		context = InstrumentationRegistry.getInstrumentation().targetContext
		val key = "PayPalUpstreamLocalStorage"
		sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
		sharedPreferences.edit().clear().apply()
	}

	private fun setTimestampToSecondsFromNow(seconds: Long) {
		val timestamp = System.currentTimeMillis() / 1000 + seconds
		sharedPreferences.edit().putLong("${LocalStorage.StorageKeys.TIMESTAMP}", timestamp).apply()
	}

	@Test
	fun testLocalStorage() {
		val localStorage = LocalStorage(context)

		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = false,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 10L,
			ttlHard = 20L,
		)

		assertEquals("1234567890", localStorage.merchantHash)
		assertEquals(false, localStorage.isCacheFlowDisabled)
		assertEquals(10L, localStorage.softTtl)
		assertEquals(20L, localStorage.hardTtl)
	}

	@Test
	fun testMerchantHashStateNoHash() {
		val localStorage = LocalStorage(context)

		val actualHashState = localStorage.getMerchantHashState()
		assertEquals(LocalStorage.State.NO_HASH, actualHashState)
	}

	@Test
	fun testMerchantHashStateHashOlderThanHardTtl() {
		val localStorage = LocalStorage(context)

		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = false,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 10L,
			ttlHard = 20L,
		)
		setTimestampToSecondsFromNow(-21)

		val actualHashState = localStorage.getMerchantHashState()
		assertEquals(LocalStorage.State.HASH_OLDER_THAN_HARD_TTL, actualHashState)
	}

	@Test
	fun testMerchantHashStateHashBetweenSoftAndHardTtl() {
		val localStorage = LocalStorage(context)

		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = false,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 10L,
			ttlHard = 20L,
		)
		setTimestampToSecondsFromNow(-15)

		val actualHashState = localStorage.getMerchantHashState()
		assertEquals(LocalStorage.State.HASH_BETWEEN_SOFT_AND_HARD_TTL, actualHashState)
	}

	@Test
	fun testMerchantHashStateHashYoungerThanSoftTtl() {
		val localStorage = LocalStorage(context)

		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = false,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 10L,
			ttlHard = 20L,
		)
		setTimestampToSecondsFromNow(-5)

		val actualHashState = localStorage.getMerchantHashState()
		assertEquals(LocalStorage.State.HASH_YOUNGER_THAN_SOFT_TTL, actualHashState)
	}

	@Test
	fun testMerchantHashStateCacheDisabled() {
		val localStorage = LocalStorage(context)

		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = true,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 10L,
			ttlHard = 20L,
		)

		val actualHashState = localStorage.getMerchantHashState()
		assertEquals(LocalStorage.State.CACHE_DISABLED, actualHashState)
	}
}
