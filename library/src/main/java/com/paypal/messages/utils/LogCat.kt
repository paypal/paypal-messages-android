package com.paypal.messages.utils

import android.util.Log

object LogCat {
	private const val prefix = "PPMessages"
	private const val SHOW_FULL_MESSAGES = false

	private fun reduceLongMessage(message: String): String {
		if (SHOW_FULL_MESSAGES) return message
		return message.split("\n").take(10).joinToString("\n")
	}

	@JvmStatic
	@JvmOverloads
	fun verbose(tag: String, message: String, throwable: Throwable? = null) {
		Log.v("$prefix:$tag", reduceLongMessage(message), throwable)
	}

	@JvmStatic
	@JvmOverloads
	fun debug(tag: String, message: String, throwable: Throwable? = null) {
		Log.d("$prefix:$tag", reduceLongMessage(message), throwable)
	}

	@JvmStatic
	@JvmOverloads
	fun warn(tag: String, message: String, throwable: Throwable? = null) {
		Log.w("$prefix:$tag", reduceLongMessage(message), throwable)
	}

	@JvmStatic
	@JvmOverloads
	fun error(tag: String, message: String, throwable: Throwable? = null) {
		Log.e("$prefix:$tag", reduceLongMessage(message), throwable)
	}
}
