package com.paypal.messages.io

import android.annotation.SuppressLint
import com.paypal.messages.utils.KoverExcludeGenerated
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
	// trustAllCerts is only used for connecting to test environments
	private val trustAllCerts = arrayOf<TrustManager>(
		@SuppressLint("CustomX509TrustManager")
		object : X509TrustManager {
			@SuppressLint("TrustAllX509TrustManager")
			@KoverExcludeGenerated
			override fun checkClientTrusted(
				chain: Array<out X509Certificate>?,
				authType: String?,
			) {}

			@SuppressLint("TrustAllX509TrustManager")
			@KoverExcludeGenerated
			override fun checkServerTrusted(
				chain: Array<out X509Certificate>?,
				authType: String?,
			) {}

			override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
		},
	)

	// sslContext is only used for connecting to test environments
	private val sslContext: SSLContext = SSLContext.getInstance("SSL")
		.apply {
			this.init(null, trustAllCerts, java.security.SecureRandom())
		}

	// sslSocketFactor is only used for connecting to test environments
	private val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

	// insecureClient is only used for connecting to test environments
	@Suppress("unused")
	internal val insecureClient = OkHttpClient.Builder()
		.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
		.hostnameVerifier { _, _ -> true }
		.build()

	internal val secureClient = OkHttpClient()
}
