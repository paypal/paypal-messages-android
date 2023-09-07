package com.paypal.messages

import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.ProductGroup

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
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_primary_grayscale
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_primary_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_primary_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_primary_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_primary_grayscale
						}
					}
				)
			}

			PayPalMessageLogoType.ALTERNATIVE -> {
				LogoAsset.ImageAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_credit_alternative_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_credit_alternative_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_credit_alternative_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_alternative_grayscale
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_alternative_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_alternative_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_alternative_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_alternative_grayscale
						}
					}
				)
			}

			PayPalMessageLogoType.INLINE -> {
				LogoAsset.ImageAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_credit_inline_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_credit_inline_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_credit_inline_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_credit_inline_grayscale
						}

						else -> when (color) {
							PayPalMessageColor.BLACK -> R.drawable.logo_inline_standard
							PayPalMessageColor.WHITE -> R.drawable.logo_inline_white
							PayPalMessageColor.MONOCHROME -> R.drawable.logo_inline_monochrome
							PayPalMessageColor.GRAYSCALE -> R.drawable.logo_inline_grayscale
						}
					}
				)
			}

			PayPalMessageLogoType.NONE -> {
				LogoAsset.StringAsset(
					when (productGroup) {
						ProductGroup.PAYPAL_CREDIT -> R.string.logo_none_label_credit
						else -> R.string.logo_none_label_default
					}
				)
			}
		}
	}
}
