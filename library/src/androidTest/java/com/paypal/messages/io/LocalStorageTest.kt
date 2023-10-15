package com.paypal.messages.io

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class LocalStorageTest {
	@Test
	fun testLocalStorage() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val localStorage = LocalStorage(context)

		// Set a value for the merchantHashData property.
		localStorage.merchantHashData = ApiHashData.Response(
			cacheFlowDisabled = false,
			merchantProfile = ApiHashData.MerchantProfile(hash = "1234567890"),
			ttlSoft = 1000L,
			ttlHard = 2000L
		)

		assertEquals("1234567890", localStorage.merchantHash)
		assertEquals(false, localStorage.isCacheFlowDisabled)
		assertEquals(1000L, localStorage.softTtl)
		assertEquals(2000L, localStorage.hardTtl)
	}
}
