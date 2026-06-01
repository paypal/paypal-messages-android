package com.paypal.messages

import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType

class Logo(
	private val logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY,
	private val productGroup: ProductGroup? = ProductGroup.PAY_LATER,
) {
	fun getAsset(color: PayPalMessageColor): LogoAsset {
		return when (logoType) {
			PayPalMessageLogoType.PRIMARY -> {
				LogoAsset.ImageAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_credit_primary_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_credit_primary_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_credit_primary_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_primary_monochrome
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_primary_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_primary_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_primary_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_primary_monochrome
						}
					},
					scale = 1.25f,
				)
			}

			PayPalMessageLogoType.ALTERNATIVE -> {
				LogoAsset.ImageAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_credit_alternative_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_credit_alternative_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_credit_alternative_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_alternative_monochrome
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_alternative_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_alternative_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_alternative_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_alternative_monochrome
						}
					},
					scale = 1.25f,
				)
			}

			PayPalMessageLogoType.INLINE -> {
				LogoAsset.ImageAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_credit_inline_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_credit_inline_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_credit_inline_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_inline_monochrome
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_inline_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_inline_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_inline_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_inline_monochrome
						}
					},
				)
			}

			PayPalMessageLogoType.NONE -> {
				LogoAsset.StringAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> R.string.logo_none_label_credit
						else -> R.string.logo_none_label_default
					},
				)
			}
		}
	}
}
