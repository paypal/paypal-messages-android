package com.paypal.messages.config

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

sealed class PayPalEnvironment {
	abstract val presentmentUrl: String
	abstract val loggerBaseUrl: String
	abstract val isProduction: Boolean

	class DevelopEnvironment(host: String, port: Int? = null) : PayPalEnvironment() {
		private val localUrl = "http://localhost:$port"
		private val isLocal = host == "localhost"
		override val presentmentUrl = if (isLocal) localUrl else "https://www.$host"
		override val loggerBaseUrl = if (isLocal) localUrl else "https://api.$host"
		override val isProduction = false
	}

	class SandboxEnvironment : PayPalEnvironment() {
		override val presentmentUrl = "https://www.sandbox.paypal.com"
		override val loggerBaseUrl = "https://api.sandbox.paypal.com"
		override val isProduction = true
	}

	class LiveEnvironment : PayPalEnvironment() {
		override val presentmentUrl = "https://www.paypal.com"
		override val loggerBaseUrl = "https://api.paypal.com"
		override val isProduction = true
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

	@Suppress("FunctionName")
	companion object {
		fun DEVELOP(host: String): DevelopEnvironment {
			return DevelopEnvironment(host)
		}
		fun DEVELOP(port: Int = 8443): DevelopEnvironment {
			return DevelopEnvironment("localhost", port)
		}

		val SANDBOX = SandboxEnvironment()
		val LIVE = LiveEnvironment()
	}
}
