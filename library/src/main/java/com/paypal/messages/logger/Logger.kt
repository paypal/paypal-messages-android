package com.paypal.messages.logger

import android.content.Context
import android.provider.Settings
import com.paypal.messages.io.Api
import com.paypal.messages.io.LocalStorage
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Logger private constructor() {
	var integrationName: String = ""
		private set
	var integrationVersion: String = ""
		private set
	var deviceId: String = ""
		private set
	var sessionId: String = ""
		private set

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

	var payload: TrackingPayload? = null
		private set

	// When we instantiate our class for the first time, there are some global vars we can set right
	// away, and don't need to rely on the message class
	init {
		initBasePayload()
	}

	private fun resetBasePayload(isInit: Boolean = false) {
		LogCat.debug(TAG, if (isInit) "initBasePayload" else "resetBasePayload")
		this.payload = TrackingPayload(
			clientId = clientId,
			merchantId = null,
			partnerAttributionId = null,
			// merchantProfileHash will be later defined by pulling from LocalStorage
			merchantProfileHash = null,
			deviceId = if (deviceId == "") Settings.Secure.ANDROID_ID else deviceId,
			// TODO Determine SessionId for Logger
			sessionId = if (deviceId == "") "random_session_id" else sessionId,
			integrationName = integrationName,
			integrationVersion = integrationVersion,
			components = mutableListOf(),
		)
	}

	private fun initBasePayload() {
		if (clientId !== "") {
			resetBasePayload(true)
		}
		else {
			val exception = PayPalErrors.InvalidClientIdException("ClientID was empty")
			exception.message?.let { LogCat.error(TAG, it, exception) }
		}
	}

	fun setGlobalAnalytics(
		integrationName: String = "",
		integrationVersion: String = "",
		deviceId: String = "",
		sessionId: String = "",
	) {
		integrationName.takeIf { it != "" }?.let {
			this.integrationName = it
			this.payload?.integrationName = it
		}
		integrationVersion.takeIf { it != "" }?.let {
			this.integrationVersion = it
			this.payload?.integrationVersion = it
		}
		deviceId.takeIf { it != "" }?.let {
			this.deviceId = it
			this.payload?.deviceId = it
		}
		sessionId.takeIf { it != "" }?.let {
			this.sessionId = it
			this.payload?.sessionId = it
		}
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
			val oldEvents = oldComponent.componentEvents
			component.componentEvents.addAll(0, oldEvents)

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

		this.payload?.merchantProfileHash = LocalStorage(context).merchantHash
		this.payload?.let { sendEvent(context, it) }
	}

	private var job: Job? = null
	private fun sendEvent(context: Context, finalPayload: TrackingPayload) {
		job?.cancel()
		job = CoroutineScope(Dispatchers.IO).launch {
			delay(5000) // Wait 5 seconds before sending our payload

			val localStorage = LocalStorage(context)
			val hash = localStorage.merchantHash
			val payloadSummary = finalPayload.components.joinToString("\n") {
				val eventsString = it.componentEvents.joinToString { event -> event.eventType.toString() }
				"  type: ${it.type}\n  instanceId: ${it.instanceId}\n  events: $eventsString\n"
			}
			LogCat.debug(TAG, "merchantHash: ${hash}\npayloadSummary:\n$payloadSummary")

			finalPayload.merchantProfileHash = hash
			Api.callLoggerEndpoint(finalPayload)
			resetBasePayload()
		}
	}
}
