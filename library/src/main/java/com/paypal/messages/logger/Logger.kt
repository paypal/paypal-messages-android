package com.paypal.messages.logger

import android.content.Context
import android.provider.Settings
import com.paypal.messages.io.LocalStorage
import com.paypal.messages.errors.InvalidCheckoutConfigException
import com.paypal.messages.io.Api
import com.paypal.messages.utils.LogCat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Logger private constructor() {
	private var integrationName: String = ""
	private var integrationVersion: String = ""

	companion object {
		private const val TAG: String = "Logger"

		@Volatile
		private var instance: Logger? = null
		private lateinit var clientId: String

		fun getInstance(clientId: String = ""): Logger {
			LogCat.debug(TAG, "getInstance clientId: $clientId")
			this.clientId = clientId
			return instance ?: synchronized(this) {
				if (instance == null) {
					instance = Logger()
				}
				instance!!
			}
		}
	}

	private var payload: TrackingPayload? = null

	// When we instantiate our class for the first time, there are some global vars we can set right
	// away, and don't need to rely on the message class
	init {
		initBasePayload()
	}

	private fun initBasePayload() {
		if (clientId !== "") {
			this.payload = TrackingPayload(
				clientId = clientId,
				merchantId = null,
				partnerAttributionId = null,
				// merchantProfileHash can be later defined after a message request by pulling from paypalmessagelocalstorage
				merchantProfileHash = null,
				deviceId = Settings.Secure.ANDROID_ID,
				// TODO Determine SessionId for Logger
				sessionId = "random for now, TBD at later point to what this is specifically",
				integrationName = integrationName,
				integrationVersion = integrationVersion,
				components = mutableListOf(),
			)
		}
		else {
			val exception = InvalidCheckoutConfigException()
			exception.message?.let { LogCat.error(TAG, it, exception) }
		}
	}

	fun setGlobalAnalytics (
		integrationName: String,
		integrationVersion: String,
	) {
		this.integrationName = integrationName
		this.integrationVersion = integrationVersion
	}

	// Need to be able to append multiple events to a single payload, adding a modal event could
	// add additional shared fields to the component level object

	fun log(context: Context, component: TrackingComponent) {
		// We have a component level scoped payload, that we need to add to our request.
		// First we determine if this is a new component or not
		// IF it is, we add it to our base payload with no issues
		// else, we need to find the correct component, and apply on top of it
		// we can use instance id to determine which existing payload we need to update
		// If we have an active call, we need to pull the current payload and modify it

		// Check for an existing component payload for this component
		val oldComponent = this.payload?.components?.find { it.instanceId == component.instanceId }

		if (oldComponent != null) {
			val oldEvents = oldComponent.events
			component.events.addAll(0, oldEvents)

			val index = this.payload?.components?.indexOfFirst { it.instanceId == component.instanceId }

			if (index != -1 && index != null) {
				// Replace the old component payload with our newly created one
				this.payload?.components?.set(index, component)
			}
		}
		else {
			// This will be the first instance for this specific component
			this.payload?.components?.add(component)
		}

		this.payload?.let { sendEvent(context, it) }
	}

	private var job: Job? = null
	private fun sendEvent(context: Context, finalPayload: TrackingPayload) {
		job?.cancel()
		job = CoroutineScope(Dispatchers.IO).launch {
			delay(5000) // Wait 5 seconds before sending our payload

			val localStorage = LocalStorage(context)
			LogCat.debug(TAG, localStorage.merchantHash.toString())
			finalPayload.components.forEach { component ->
				component.instanceId?.let { LogCat.debug(TAG, it) }
				component.events.forEach { event ->
					LogCat.debug(TAG, event.eventType.toString())
				}
			}

			Api.callLoggerEndpoint(finalPayload)

			// After our response has completed, we can reset our base payload.
			initBasePayload()
		}
	}
}
