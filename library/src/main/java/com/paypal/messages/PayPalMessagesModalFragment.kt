package com.paypal.messages

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.use
import com.paypal.messages.analytics.AnalyticsComponent
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.analytics.AnalyticsLogger
import com.paypal.messages.analytics.ComponentType
import com.paypal.messages.analytics.EventType
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.io.Api
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import com.paypal.messages.config.modal.PayPalMessagesModalConfig as MessagesModalConfig

/**
 * PayPalMessagesModal is a component that provides the merchant with the option to attach a modal
 * to their own content with different pay later products offered by PayPal.
 * The content and information is based on the provided fields.
 * Interacting with their content will show more information about the
 * product itself and the option to apply
 */
class PayPalMessagesModalFragment @JvmOverloads constructor(
	context: Context,
	attributeSet: AttributeSet? = null,
	defStyleAttr: Int = 0,
	config: MessagesModalConfig = MessagesModalConfig(clientID = ""),
) : FrameLayout(context, attributeSet, defStyleAttr) {
	private val TAG = "PayPalMessagesModalFragment"
	private var instanceId = UUID.randomUUID()

	fun getConfig(): MessagesModalConfig {
		return MessagesModalConfig(
			clientID = this.clientID,
			merchantID = this.merchantID,
			partnerAttributionID = this.partnerAttributionID,
			amount = this.amount,
			buyerCountry = this.buyerCountry,
			offerType = this.offerType,
			environment = this.environment ?: PayPalEnvironment.SANDBOX,
			callbacks = ModalEvents(
				onClick = this.onClick,
				onApply = this.onApply,
				onLoading = this.onLoading,
				onSuccess = this.onSuccess,
				onError = this.onError,
				onCalculate = this.onCalculate,
				onShow = this.onShow,
				onClose = this.onClose,
			),
		)
	}

	fun setConfig(config: MessagesModalConfig) {
		clientID = config.clientID
		merchantID = config.merchantID
		partnerAttributionID = config.partnerAttributionID
		amount = config.amount
		buyerCountry = config.buyerCountry
		offerType = config.offerType
		onClick = config.callbacks?.onClick ?: {}
		onApply = config.callbacks?.onApply ?: {}
		onLoading = config.callbacks?.onLoading ?: {}
		onSuccess = config.callbacks?.onSuccess ?: {}
		onError = config.callbacks?.onError ?: {}
		onCalculate = config.callbacks?.onCalculate ?: {}
		onShow = config.callbacks?.onShow ?: {}
		onClose = config.callbacks?.onClose ?: {}
		debounceUpdateContent(Unit)
	}

	private fun <T> debounce(
		delayMs: Long = 1L,
		coroutineContext: CoroutineContext = Dispatchers.Main,
		callback: (T) -> Unit,
	): (T) -> Unit {
		var debounceJob: Job? = null
		return { param: T ->
			if (debounceJob?.isCompleted != false) {
				debounceJob = CoroutineScope(coroutineContext).launch {
					delay(delayMs)
					callback(param)
				}
			}
		}
	}

	// This must be above the set methods to prevent errors when using XML attributes
	val debounceUpdateContent = debounce<Unit> { }

	var clientID: String = config.clientID
		set(arg) {
			if (arg === "") LogCat.error(TAG, "ClientID is an empty string")
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var merchantID: String? = config.merchantID
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var partnerAttributionID: String? = config.partnerAttributionID
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var amount: Double? = config.amount
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.amount = field
				debounceUpdateContent(Unit)
			}
		}
	var buyerCountry: String? = config.buyerCountry
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.buyerCountry = field
				debounceUpdateContent(Unit)
			}
		}
	var channel: String? = config.channel
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.channel = field
				debounceUpdateContent(Unit)
			}
		}
	var offerType: PayPalMessageOfferType? = config.offerType
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.offerType = field
				debounceUpdateContent(Unit)
			}
		}
	var pageType: PayPalMessagePageType? = config.pageType
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var environment: PayPalEnvironment? = config.environment
		set(arg) {
			if (field != arg) {
				field = arg
				Api.env = arg ?: PayPalEnvironment.SANDBOX
				debounceUpdateContent(Unit)
			}
		}

	// CALLBACKS
	var onClick: () -> Unit = config.callbacks?.onClick ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onApply: () -> Unit = config.callbacks?.onApply ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onLoading: () -> Unit = config.callbacks?.onLoading ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onSuccess: () -> Unit = config.callbacks?.onSuccess ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onError: (error: PayPalErrors.Base) -> Unit = config.callbacks?.onError ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onCalculate: () -> Unit = config.callbacks?.onCalculate ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onShow: () -> Unit = config.callbacks?.onShow ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onClose: () -> Unit = config.callbacks?.onClose ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}

	private var modal: ModalFragment? = null

	init {
		context.obtainStyledAttributes(attributeSet, R.styleable.PayPalMessagesModalFragment).use { typedArray ->
			updateFromAttributes(typedArray)
		}
		if (config.clientID === "") LogCat.error(TAG, "ClientID is an empty string")
	}

	fun show() {
		// Since show is called as part of a merchant's onClickListener, we log that event here
		logEvent(
			AnalyticsEvent(
				eventType = EventType.STANDALONE_MODAL_CLICKED,
				pageViewLinkName = "Standalone modal, text set by merchant",
				pageViewLinkSource = "merchant_text",
			),
		)
		val modal = modal ?: run {
			val modal = ModalFragment(clientID)
			// Build modal config
			val modalConfig = ModalConfig(
				amount = this.amount,
				buyerCountry = this.buyerCountry,
				offer = this.offerType,
				ignoreCache = false,
				devTouchpoint = false,
				stageTag = null,
				events = ModalEvents(
					onApply = this.onApply,
					onClick = this.onClick,
					onError = this.onError,
				),
				modalCloseButton = ModalCloseButton(),
			)

			modal.init(modalConfig)
			modal.show((context as AppCompatActivity).supportFragmentManager, modal.tag)

			this.modal = modal

			modal
		}

		// modal.show() above will display the modal on initial view, but if the user closes the modal
		// it will become visually hidden and this method will re-display the modal without
		// attempting to reattach it
		// the delay prevents noticeable shift when the offer type is changed
		Handler(Looper.getMainLooper()).postDelayed({
			modal.expand()
		}, 250)
	}

	fun hide() {
		modal?.hide()
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		// The modal will not dismiss (destroy) itself, it will only hide/show when opening and closing
		// so we need to cleanup the modal instance if the message is removed
		this.modal?.dismiss()
	}

	/**
	 * This function will update the local config related values based on what is provided from the [PayPalMessageView] xml custom view.
	 */
	private fun updateFromAttributes(typedArray: TypedArray) {
		if (typedArray.hasValue(R.styleable.PayPalMessagesModalFragment_paypal_client_id)) {
			clientID = typedArray.getString(R.styleable.PayPalMessagesModalFragment_paypal_client_id).toString()
		}

		if (typedArray.hasValue(R.styleable.PayPalMessagesModalFragment_paypal_amount)) {
			amount = try {
				typedArray.getFloatOrThrow(R.styleable.PayPalMessagesModalFragment_paypal_amount).toDouble()
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing amount attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessagesModalFragment_paypal_offer_type)) {
			offerType = try {
				PayPalMessageOfferType(typedArray.getIntOrThrow(R.styleable.PayPalMessagesModalFragment_paypal_offer_type))
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing offer_type attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessagesModalFragment_paypal_page_type)) {
			pageType = try {
				PayPalMessagePageType(typedArray.getIntOrThrow(R.styleable.PayPalMessagesModalFragment_paypal_page_type))
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing page_type attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessagesModalFragment_paypal_buyer_country)) {
			buyerCountry = typedArray.getString(R.styleable.PayPalMessagesModalFragment_paypal_buyer_country)
		}
	}

	private fun logEvent(event: AnalyticsEvent) {
		val component = AnalyticsComponent(
			offerType = this.offerType,
			amount = this.amount.toString(),
			pageType = this.pageType,
			buyerCountryCode = this.buyerCountry,
			instanceId = this.instanceId.toString(),
			originatingInstanceId = Api.originatingInstanceId.toString(),
			type = ComponentType.STANDALONE_MODAL.toString(),
			componentEvents = mutableListOf(event),
		)

		AnalyticsLogger.getInstance(clientId = clientID).log(context, component)
	}
}
