package com.paypal.messagesdemo

import com.paypal.messages.config.PayPalEnvironment as Environment

object EnvVars {
	private val clientIds = mapOf(
		Environment.LIVE to BuildConfig.PROD_CLIENT_ID,
		Environment.SANDBOX to BuildConfig.SANDBOX_CLIENT_ID,
		Environment.STAGE to BuildConfig.STAGE_CLIENT_ID,
		Environment.LOCAL to BuildConfig.LOCAL_CLIENT_ID,
	)

	fun getClientId(environment: Environment): String {
		return clientIds[environment] ?: ""
	}
}
