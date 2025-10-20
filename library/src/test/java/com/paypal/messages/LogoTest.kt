package com.paypal.messages

import com.paypal.messages.config.ProductGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

class LogoTest {
	@ParameterizedTest
	@MethodSource("logoImageArguments")
	fun testLogoImage(
		logoType: LogoType,
		productGroup: ProductGroup?,
		color: Color,
		expectedResId: Int,
	) {
		val logo = Logo(logoType, productGroup)
		val asset = logo.getAsset(color) as LogoAsset.ImageAsset

		assertEquals(expectedResId, asset.resId)
	}

	@ParameterizedTest
	@MethodSource("logoStringArguments")
	fun testStringImage(
		logoType: LogoType,
		productGroup: ProductGroup?,
		expectedResId: Int,
	) {
		val logo = Logo(logoType, productGroup)
		val asset = logo.getAsset(Color.BLACK) as LogoAsset.StringAsset

		assertEquals(expectedResId, asset.resId)
	}

	private companion object {
		@JvmStatic
		fun logoImageArguments(): Stream<Arguments> = Stream.of(
			// PRIMARY CREDIT
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAYPAL_CREDIT,
				Color.BLACK,
				R.drawable.logo_credit_primary_standard,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAYPAL_CREDIT,
				Color.WHITE,
				R.drawable.logo_credit_primary_white,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAYPAL_CREDIT,
				Color.MONOCHROME,
				R.drawable.logo_credit_primary_monochrome,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAYPAL_CREDIT,
				Color.GRAYSCALE,
				R.drawable.logo_credit_primary_grayscale,
			),

			// PRIMARY PAY LATER
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAY_LATER,
				Color.BLACK,
				R.drawable.logo_primary_standard,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAY_LATER,
				Color.WHITE,
				R.drawable.logo_primary_white,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAY_LATER,
				Color.MONOCHROME,
				R.drawable.logo_primary_monochrome,
			),
			Arguments.of(
				LogoType.PRIMARY,
				ProductGroup.PAY_LATER,
				Color.GRAYSCALE,
				R.drawable.logo_primary_grayscale,
			),
			Arguments.of(
				LogoType.PRIMARY,
				null,
				Color.BLACK,
				R.drawable.logo_primary_standard,
			),

			// ALTERNATIVE CREDIT
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAYPAL_CREDIT,
				Color.BLACK,
				R.drawable.logo_credit_alternative_standard,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAYPAL_CREDIT,
				Color.WHITE,
				R.drawable.logo_credit_alternative_white,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAYPAL_CREDIT,
				Color.MONOCHROME,
				R.drawable.logo_credit_alternative_monochrome,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAYPAL_CREDIT,
				Color.GRAYSCALE,
				R.drawable.logo_credit_alternative_grayscale,
			),

			// ALTERNATIVE PAY LATER
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAY_LATER,
				Color.BLACK,
				R.drawable.logo_alternative_standard,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAY_LATER,
				Color.WHITE,
				R.drawable.logo_alternative_white,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAY_LATER,
				Color.MONOCHROME,
				R.drawable.logo_alternative_monochrome,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				ProductGroup.PAY_LATER,
				Color.GRAYSCALE,
				R.drawable.logo_alternative_grayscale,
			),
			Arguments.of(
				LogoType.ALTERNATIVE,
				null,
				Color.BLACK,
				R.drawable.logo_alternative_standard,
			),

			// INLINE CREDIT
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAYPAL_CREDIT,
				Color.BLACK,
				R.drawable.logo_credit_inline_standard,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAYPAL_CREDIT,
				Color.WHITE,
				R.drawable.logo_credit_inline_white,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAYPAL_CREDIT,
				Color.MONOCHROME,
				R.drawable.logo_credit_inline_monochrome,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAYPAL_CREDIT,
				Color.GRAYSCALE,
				R.drawable.logo_credit_inline_grayscale,
			),

			// INLINE PAY LATER
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAY_LATER,
				Color.BLACK,
				R.drawable.logo_inline_standard,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAY_LATER,
				Color.WHITE,
				R.drawable.logo_inline_white,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAY_LATER,
				Color.MONOCHROME,
				R.drawable.logo_inline_monochrome,
			),
			Arguments.of(
				LogoType.INLINE,
				ProductGroup.PAY_LATER,
				Color.GRAYSCALE,
				R.drawable.logo_inline_grayscale,
			),
			Arguments.of(
				LogoType.INLINE,
				null,
				Color.BLACK,
				R.drawable.logo_inline_standard,
			),
		)

		@JvmStatic
		fun logoStringArguments(): Stream<Arguments> = Stream.of(
			Arguments.of(LogoType.NONE, ProductGroup.PAYPAL_CREDIT, R.string.logo_none_label_credit),
			Arguments.of(LogoType.NONE, ProductGroup.PAY_LATER, R.string.logo_none_label_default),
			Arguments.of(LogoType.NONE, null, R.string.logo_none_label_default),
		)
	}

	@org.junit.jupiter.api.Test
	fun testLogoDefaultConstructor() {
		// Test that Logo uses defaults when no parameters provided
		val logo = Logo()
		val asset = logo.getAsset(Color.BLACK)

		// Should default to PRIMARY logo with PAY_LATER product group
		assertEquals(LogoAsset.ImageAsset(R.drawable.logo_primary_standard), asset)
	}

	@org.junit.jupiter.api.Test
	fun testLogoWithOnlyLogoType() {
		// Test Logo with only logoType specified
		val logo = Logo(logoType = LogoType.ALTERNATIVE)
		val asset = logo.getAsset(Color.BLACK)

		// Should use ALTERNATIVE with default PAY_LATER
		assertEquals(LogoAsset.ImageAsset(R.drawable.logo_alternative_standard), asset)
	}

	@org.junit.jupiter.api.Test
	fun testLogoWithOnlyProductGroup() {
		// Test Logo with only productGroup specified
		val logo = Logo(productGroup = ProductGroup.PAYPAL_CREDIT)
		val asset = logo.getAsset(Color.BLACK)

		// Should use default PRIMARY with PAYPAL_CREDIT
		assertEquals(LogoAsset.ImageAsset(R.drawable.logo_credit_primary_standard), asset)
	}

	@org.junit.jupiter.api.Test
	fun testAllColorVariantsForPrimary() {
		// Ensure all color variants work for primary logo
		val logo = Logo(LogoType.PRIMARY, ProductGroup.PAY_LATER)

		val blackAsset = logo.getAsset(Color.BLACK) as LogoAsset.ImageAsset
		val whiteAsset = logo.getAsset(Color.WHITE) as LogoAsset.ImageAsset
		val monochromeAsset = logo.getAsset(Color.MONOCHROME) as LogoAsset.ImageAsset
		val grayscaleAsset = logo.getAsset(Color.GRAYSCALE) as LogoAsset.ImageAsset

		assertEquals(R.drawable.logo_primary_standard, blackAsset.resId)
		assertEquals(R.drawable.logo_primary_white, whiteAsset.resId)
		assertEquals(R.drawable.logo_primary_monochrome, monochromeAsset.resId)
		assertEquals(R.drawable.logo_primary_grayscale, grayscaleAsset.resId)
	}

	@org.junit.jupiter.api.Test
	fun testAllColorVariantsForAlternative() {
		// Ensure all color variants work for alternative logo
		val logo = Logo(LogoType.ALTERNATIVE, ProductGroup.PAY_LATER)

		val blackAsset = logo.getAsset(Color.BLACK) as LogoAsset.ImageAsset
		val whiteAsset = logo.getAsset(Color.WHITE) as LogoAsset.ImageAsset
		val monochromeAsset = logo.getAsset(Color.MONOCHROME) as LogoAsset.ImageAsset
		val grayscaleAsset = logo.getAsset(Color.GRAYSCALE) as LogoAsset.ImageAsset

		assertEquals(R.drawable.logo_alternative_standard, blackAsset.resId)
		assertEquals(R.drawable.logo_alternative_white, whiteAsset.resId)
		assertEquals(R.drawable.logo_alternative_monochrome, monochromeAsset.resId)
		assertEquals(R.drawable.logo_alternative_grayscale, grayscaleAsset.resId)
	}

	@org.junit.jupiter.api.Test
	fun testAllColorVariantsForInline() {
		// Ensure all color variants work for inline logo
		val logo = Logo(LogoType.INLINE, ProductGroup.PAY_LATER)

		val blackAsset = logo.getAsset(Color.BLACK) as LogoAsset.ImageAsset
		val whiteAsset = logo.getAsset(Color.WHITE) as LogoAsset.ImageAsset
		val monochromeAsset = logo.getAsset(Color.MONOCHROME) as LogoAsset.ImageAsset
		val grayscaleAsset = logo.getAsset(Color.GRAYSCALE) as LogoAsset.ImageAsset

		assertEquals(R.drawable.logo_inline_standard, blackAsset.resId)
		assertEquals(R.drawable.logo_inline_white, whiteAsset.resId)
		assertEquals(R.drawable.logo_inline_monochrome, monochromeAsset.resId)
		assertEquals(R.drawable.logo_inline_grayscale, grayscaleAsset.resId)
	}

	@org.junit.jupiter.api.Test
	fun testNoneLogoReturnsStringAsset() {
		// Test that NONE logo type returns StringAsset, not ImageAsset
		val logo = Logo(LogoType.NONE, ProductGroup.PAY_LATER)
		val asset = logo.getAsset(Color.BLACK)

		// Verify it's a StringAsset
		assertTrue(asset is LogoAsset.StringAsset)
		assertEquals(R.string.logo_none_label_default, (asset as LogoAsset.StringAsset).resId)
	}

	@org.junit.jupiter.api.Test
	fun testImageLogoTypesReturnImageAsset() {
		// Test that non-NONE logo types return ImageAsset
		val logoTypes = listOf(LogoType.PRIMARY, LogoType.ALTERNATIVE, LogoType.INLINE)

		logoTypes.forEach { logoType ->
			val logo = Logo(logoType, ProductGroup.PAY_LATER)
			val asset = logo.getAsset(Color.BLACK)

			assertTrue(asset is LogoAsset.ImageAsset, "Logo type $logoType should return ImageAsset")
		}
	}

	@org.junit.jupiter.api.Test
	fun testCreditVsPayLaterLogos() {
		// Test that PAYPAL_CREDIT and PAY_LATER use different logos
		val creditLogo = Logo(LogoType.PRIMARY, ProductGroup.PAYPAL_CREDIT)
		val payLaterLogo = Logo(LogoType.PRIMARY, ProductGroup.PAY_LATER)

		val creditAsset = creditLogo.getAsset(Color.BLACK) as LogoAsset.ImageAsset
		val payLaterAsset = payLaterLogo.getAsset(Color.BLACK) as LogoAsset.ImageAsset

		// Should be different resource IDs
		assertTrue(creditAsset.resId != payLaterAsset.resId)
		assertEquals(R.drawable.logo_credit_primary_standard, creditAsset.resId)
		assertEquals(R.drawable.logo_primary_standard, payLaterAsset.resId)
	}

	@org.junit.jupiter.api.Test
	fun testNullProductGroupDefaultsToPayLater() {
		// Test that null product group behaves like PAY_LATER
		val nullProductLogo = Logo(LogoType.PRIMARY, null)
		val payLaterLogo = Logo(LogoType.PRIMARY, ProductGroup.PAY_LATER)

		val nullAsset = nullProductLogo.getAsset(Color.BLACK) as LogoAsset.ImageAsset
		val payLaterAsset = payLaterLogo.getAsset(Color.BLACK) as LogoAsset.ImageAsset

		assertEquals(payLaterAsset.resId, nullAsset.resId)
	}
}
