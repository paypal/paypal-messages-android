package com.paypal.messages

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

	@Test
	fun testStringAssetWithDifferentResIds() {
		// Test that different string resources create different assets
		val creditAsset = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val defaultAsset = LogoAsset.StringAsset(R.string.logo_none_label_default)

		assertNotEquals(creditAsset.resId, defaultAsset.resId)
	}

	@Test
	fun testImageAssetWithDifferentResIds() {
		// Test that different drawable resources create different assets
		val standardAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)
		val whiteAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_white)

		assertNotEquals(standardAsset.resId, whiteAsset.resId)
	}

	@Test
	fun testStringAssetEquality() {
		// Test that StringAssets with the same resId are equal
		val asset1 = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val asset2 = LogoAsset.StringAsset(R.string.logo_none_label_credit)

		assertEquals(asset1, asset2)
		assertEquals(asset1.resId, asset2.resId)
	}

	@Test
	fun testImageAssetEquality() {
		// Test that ImageAssets with the same resId are equal
		val asset1 = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)
		val asset2 = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)

		assertEquals(asset1, asset2)
		assertEquals(asset1.resId, asset2.resId)
	}

	@Test
	fun testStringAssetInequality() {
		// Test that StringAssets with different resIds are not equal
		val asset1 = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val asset2 = LogoAsset.StringAsset(R.string.logo_none_label_default)

		assertNotEquals(asset1, asset2)
	}

	@Test
	fun testImageAssetInequality() {
		// Test that ImageAssets with different resIds are not equal
		val asset1 = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)
		val asset2 = LogoAsset.ImageAsset(R.drawable.logo_primary_white)

		assertNotEquals(asset1, asset2)
	}

	@Test
	fun testStringAssetIsLogoAsset() {
		// Test that StringAsset is a subclass of LogoAsset
		val stringAsset = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		assertTrue(stringAsset is LogoAsset)
	}

	@Test
	fun testImageAssetIsLogoAsset() {
		// Test that ImageAsset is a subclass of LogoAsset
		val imageAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)
		assertTrue(imageAsset is LogoAsset)
	}

	@Test
	fun testSealedClassHierarchy() {
		// Test that both StringAsset and ImageAsset extend LogoAsset
		val stringAsset: LogoAsset = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val imageAsset: LogoAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)

		// Verify type hierarchy
		assertTrue(stringAsset is LogoAsset.StringAsset)
		assertTrue(imageAsset is LogoAsset.ImageAsset)
	}

	@Test
	fun testWhenExpressionCoverage() {
		// Test that sealed class works with when expressions
		val stringAsset: LogoAsset = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val imageAsset: LogoAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)

		val stringResult = when (stringAsset) {
			is LogoAsset.StringAsset -> "string"
			is LogoAsset.ImageAsset -> "image"
		}

		val imageResult = when (imageAsset) {
			is LogoAsset.StringAsset -> "string"
			is LogoAsset.ImageAsset -> "image"
		}

		assertEquals("string", stringResult)
		assertEquals("image", imageResult)
	}

	@Test
	fun testCopyOfDataClass() {
		// Test that data class copy works for StringAsset
		val original = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val copied = original.copy(resId = R.string.logo_none_label_default)

		assertNotEquals(original, copied)
		assertEquals(R.string.logo_none_label_credit, original.resId)
		assertEquals(R.string.logo_none_label_default, copied.resId)
	}

	@Test
	fun testCopyOfImageDataClass() {
		// Test that data class copy works for ImageAsset
		val original = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)
		val copied = original.copy(resId = R.drawable.logo_primary_white)

		assertNotEquals(original, copied)
		assertEquals(R.drawable.logo_primary_standard, original.resId)
		assertEquals(R.drawable.logo_primary_white, copied.resId)
	}

	@Test
	fun testHashCodeConsistency() {
		// Test that equal objects have the same hash code
		val asset1 = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val asset2 = LogoAsset.StringAsset(R.string.logo_none_label_credit)

		assertEquals(asset1.hashCode(), asset2.hashCode())
	}

	@Test
	fun testToStringOutput() {
		// Test that toString provides useful output
		val stringAsset = LogoAsset.StringAsset(R.string.logo_none_label_credit)
		val imageAsset = LogoAsset.ImageAsset(R.drawable.logo_primary_standard)

		val stringOutput = stringAsset.toString()
		val imageOutput = imageAsset.toString()

		assertTrue(stringOutput.contains("StringAsset"))
		assertTrue(stringOutput.contains("resId"))
		assertTrue(imageOutput.contains("ImageAsset"))
		assertTrue(imageOutput.contains("resId"))
	}
}
