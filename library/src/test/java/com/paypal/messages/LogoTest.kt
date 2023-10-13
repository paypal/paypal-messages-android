package com.paypal.messages

import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class LogoTest {
	@ParameterizedTest
	@MethodSource("logoImageArguments")
	fun testLogoImage(
		logoType: LogoType,
		productGroup: ProductGroup,
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
		productGroup: ProductGroup,
		expectedResId: Int,
	) {
		val logo = Logo(logoType, productGroup)
		val asset = logo.getAsset(Color.BLACK) as LogoAsset.StringAsset

		assertEquals(expectedResId, asset.resId)
	}

	private companion object {
		@JvmStatic
		fun logoImageArguments(): Stream<Arguments> = Stream.of(
			// PRIMARY
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAYPAL_CREDIT, Color.BLACK,
				R.drawable.logo_credit_primary_standard
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAYPAL_CREDIT, Color.WHITE,
				R.drawable.logo_credit_primary_white
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAYPAL_CREDIT, Color.MONOCHROME,
				R.drawable.logo_credit_primary_monochrome
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAYPAL_CREDIT, Color.GRAYSCALE,
				R.drawable.logo_credit_primary_grayscale
			),

			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAY_LATER, Color.BLACK,
				R.drawable.logo_primary_standard
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAY_LATER, Color.WHITE,
				R.drawable.logo_primary_white
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAY_LATER, Color.MONOCHROME,
				R.drawable.logo_primary_monochrome
			),
			Arguments.of(
				LogoType.PRIMARY, ProductGroup.PAY_LATER, Color.GRAYSCALE,
				R.drawable.logo_primary_grayscale
			),

			// ALTERNATIVE
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAYPAL_CREDIT, Color.BLACK,
				R.drawable.logo_credit_alternative_standard
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAYPAL_CREDIT, Color.WHITE,
				R.drawable.logo_credit_alternative_white
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAYPAL_CREDIT, Color.MONOCHROME,
				R.drawable.logo_credit_alternative_monochrome
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAYPAL_CREDIT, Color.GRAYSCALE,
				R.drawable.logo_credit_alternative_grayscale
			),

			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAY_LATER, Color.BLACK,
				R.drawable.logo_alternative_standard
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAY_LATER, Color.WHITE,
				R.drawable.logo_alternative_white
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAY_LATER, Color.MONOCHROME,
				R.drawable.logo_alternative_monochrome
			),
			Arguments.of(
				LogoType.ALTERNATIVE, ProductGroup.PAY_LATER, Color.GRAYSCALE,
				R.drawable.logo_alternative_grayscale
			),

			// INLINE
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAYPAL_CREDIT, Color.BLACK,
				R.drawable.logo_credit_inline_standard
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAYPAL_CREDIT, Color.WHITE,
				R.drawable.logo_credit_inline_white
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAYPAL_CREDIT, Color.MONOCHROME,
				R.drawable.logo_credit_inline_monochrome
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAYPAL_CREDIT, Color.GRAYSCALE,
				R.drawable.logo_credit_inline_grayscale
			),

			Arguments.of(
				LogoType.INLINE, ProductGroup.PAY_LATER, Color.BLACK,
				R.drawable.logo_inline_standard
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAY_LATER, Color.WHITE,
				R.drawable.logo_inline_white
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAY_LATER, Color.MONOCHROME,
				R.drawable.logo_inline_monochrome
			),
			Arguments.of(
				LogoType.INLINE, ProductGroup.PAY_LATER, Color.GRAYSCALE,
				R.drawable.logo_inline_grayscale
			),
		)

		@JvmStatic
		fun logoStringArguments(): Stream<Arguments> = Stream.of(
			Arguments.of(LogoType.NONE, ProductGroup.PAYPAL_CREDIT, R.string.logo_none_label_credit),
			Arguments.of(LogoType.NONE, ProductGroup.PAY_LATER, R.string.logo_none_label_default),
		)
	}
}
