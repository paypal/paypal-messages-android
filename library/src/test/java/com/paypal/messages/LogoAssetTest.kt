package com.paypal.messages

import org.junit.Assert.assertEquals
import org.junit.Test

class LogoAssetTest {

	@Test
	fun testStringAssetConstructor() {
		val resId = R.string.logo_none_label_credit

		val stringAsset = LogoAsset.StringAsset(resId = resId)

		assertEquals(resId, stringAsset.resId)
	}

	@Test
	fun testImageAssetConstructor() {
		val resId = R.drawable.logo_alternative_grayscale

		val imageAsset = LogoAsset.ImageAsset(resId = resId)

		assertEquals(resId, imageAsset.resId)
	}
}
