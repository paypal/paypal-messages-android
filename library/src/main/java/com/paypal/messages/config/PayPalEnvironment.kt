package com.paypal.messages.config

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class PayPalEnvironment {
	LOCAL,
	STAGE,
	SANDBOX,
	LIVE,
	;

	val isProduction: Boolean
		get() = this == LIVE || this == SANDBOX

	private var host: String? = null
	private var port: Int? = null

	companion object {
		fun local(port: Int = 8443): PayPalEnvironment {
			LOCAL.port = port
			return LOCAL
		}
		fun stage(host: String): PayPalEnvironment {
			STAGE.host = host
			return STAGE
		}
	}

	private val presentmentUrl: String
		get() = when (this) {
			LOCAL -> "http://localhost:$port"
			STAGE -> "https://www.$host"
			SANDBOX -> "https://www.sandbox.paypal.com"
			LIVE -> "https://www.paypal.com"
		}

	private val loggerBaseUrl: String
		get() = when (this) {
			LOCAL -> presentmentUrl
			STAGE -> "https://api.$host"
			SANDBOX -> "https://api.sandbox.paypal.com"
			LIVE -> "https://api.paypal.com"
		}

	enum class Endpoints(val path: String) {
		MESSAGE_DATA("credit-presentment/native/message"),
		MODAL_DATA("credit-presentment/lander/modal"),
		MERCHANT_PROFILE("credit-presentment/merchant-profile"),
		LOGGER("v1/credit/upstream-messaging-events"),
	}

	fun url(endpoint: Endpoints): HttpUrl {
		val baseUrl = if (endpoint === Endpoints.LOGGER) loggerBaseUrl else presentmentUrl
		return "$baseUrl/${endpoint.path}".toHttpUrl()
	}
}
