package com.paypal.messages

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class LogoAsset {
	/**
	 * [StringAsset] is a representation of a [LogoAsset] that holds a StringRes resourceId
	 */
	data class StringAsset(@StringRes val resId: Int) : LogoAsset()

	/**
	 * [ImageAsset] is a representation of a [LogoAsset] that holds a DrawableRes resourceId
	 */
	data class ImageAsset(@DrawableRes val resId: Int) : LogoAsset()
}
