package com.paypal.messages

import android.content.Context
import android.content.res.TypedArray
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.use
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.logger.ComponentType
import com.paypal.messages.logger.EventType
import com.paypal.messages.logger.Logger
import com.paypal.messages.logger.TrackingComponent
import com.paypal.messages.logger.TrackingEvent
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis
import com.paypal.messages.config.PayPalMessageOfferType as OfferType
import com.paypal.messages.config.PayPalMessagePageType as PageType
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig
import com.paypal.messages.config.message.PayPalMessageData as MessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks as EventsCallbacks
import com.paypal.messages.config.message.PayPalMessageStyle as MessageStyle
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks as ViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlign as Align
import com.paypal.messages.config.message.style.PayPalMessageColor as Color
import com.paypal.messages.config.message.style.PayPalMessageLogoType as LogoType

/**
 * PayPalMessage is a component that provides the merchant with a message about different pay later
 * products offered by PayPal. The content and information is based on the provided fields and it can
 * be displayed in different styles. Interacting with this component will show more information about the
 * product itself and the option to apply
 */
class PayPalMessageView @JvmOverloads constructor(
	context: Context,
	attributeSet: AttributeSet? = null,
	defStyleAttr: Int = 0,
	config: MessageConfig = MessageConfig(MessageData(clientID = "")),
) : FrameLayout(context, attributeSet, defStyleAttr), OnActionCompleted {
	private val TAG = "PayPalMessage"
	private var messageTextView: TextView
	private var updateInProgress = false
	private var instanceId = UUID.randomUUID()

	fun getConfig(): MessageConfig {
		return MessageConfig(
			data = MessageData(
				clientID = this.clientID,
				merchantID = this.merchantID,
				partnerAttributionID = this.partnerAttributionID,
				amount = this.amount,
				buyerCountry = this.buyerCountry,
				offerType = this.offerType,
				pageType = this.pageType,
			),
			style = MessageStyle(this.color, this.logoType, this.textAlign),
			viewStateCallbacks = ViewStateCallbacks(this.onLoading, this.onSuccess, this.onError),
			eventsCallbacks = EventsCallbacks(this.onClick, this.onApply),
		)
	}

	fun setConfig(config: MessageConfig) {
		clientID = config.data.clientID
		merchantID = config.data.merchantID
		partnerAttributionID = config.data.partnerAttributionID
		amount = config.data.amount
		buyerCountry = config.data.clientID
		offerType = config.data.offerType
		pageType = config.data.pageType
		color = config.style.color
		logoType = config.style.logoType
		textAlign = config.style.textAlign
		onLoading = config.viewStateCallbacks?.onLoading ?: {}
		onSuccess = config.viewStateCallbacks?.onSuccess ?: {}
		onError = config.viewStateCallbacks?.onError ?: {}
		onClick = config.eventsCallbacks?.onClick ?: {}
		onApply = config.eventsCallbacks?.onApply ?: {}
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
	val debounceUpdateContent = debounce<Unit> { updateMessageContent() }

	/**
	 * DATA
	 */
	var clientID: String = config.data.clientID
		set(arg) {
			if (arg === "") LogCat.error(TAG, "ClientID is an empty string")
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var merchantID: String? = config.data.merchantID
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var partnerAttributionID: String? = config.data.partnerAttributionID
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var amount: Double? = config.data.amount
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.amount = field
				debounceUpdateContent(Unit)
			}
		}
	var buyerCountry: String? = config.data.buyerCountry
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.buyerCountry = field
				debounceUpdateContent(Unit)
			}
		}
	var offerType: OfferType? = config.data.offerType
		set(arg) {
			if (field != arg) {
				field = arg
				if (modal != null) modal?.offerType = field
				debounceUpdateContent(Unit)
			}
		}
	var pageType: PageType? = config.data.pageType
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var environment: PayPalEnvironment? = config.data.environment
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}

	/**
	 * STYLE
	 */
	var color: Color = config.style.color
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var logoType: LogoType = config.style.logoType
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var textAlign: Align = config.style.textAlign
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}

	// VIEW STATE CALLBACKS
	// Updates the specific view state callbacks for the current PayPalMessageView
	var onLoading: () -> Unit = config.viewStateCallbacks?.onLoading ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onSuccess: () -> Unit = config.viewStateCallbacks?.onSuccess ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onError: (error: PayPalErrors.Base) -> Unit = config.viewStateCallbacks?.onError ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}

	// EVENTS CALLBACKS
	// Updates the specific events callbacks for the current PayPalMessageView
	var onClick: () -> Unit = config.eventsCallbacks?.onClick ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var onApply: () -> Unit = config.eventsCallbacks?.onApply ?: {}
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}

	// Full Message Data
	private var messageDataResponse: ApiMessageData.Response? = null

	// Message Content
	private var logo = Logo()
	private var messageContent: String? = null
	private var messageDisclaimer: String? = null
	private var messageLogoTag: String? = null

	// Modal Instance
	private var modal: ModalFragment? = null

	// Stats
	private var requestDuration: Int? = null

	init {
		LayoutInflater.from(context).inflate(R.layout.paypal_message_view, this, true)

		messageTextView = findViewById(R.id.content)

		context.obtainStyledAttributes(attributeSet, R.styleable.PayPalMessageView).use { typedArray ->
			updateFromAttributes(typedArray)
		}
		if (config.data.clientID === "") LogCat.error(TAG, "ClientID is an empty string")
		Api.sessionId = UUID.randomUUID()
		updateMessageContent()
	}

	private fun showWebView(response: ApiMessageData.Response) {
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
				modalCloseButton = response.meta?.modalCloseButton!!,
			)

			modal.init(modalConfig)
			modal.show((context as AppCompatActivity).supportFragmentManager, modal.tag)

			this.modal = modal

			modal
		}

		// modal.show() above will display the modal on initial view, but if the user closes the modal
		// it will become visually hidden and this method will re-display the modal without
		// attempting to reattach it
		modal.expand()
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		// The modal will not dismiss (destroy) itself, it will only hide/show when opening and closing
		// so we need to cleanup the modal instance if the message is removed
		this.modal?.dismiss()
	}

	/**
	 * This function purpose is to update only the UI of the [PayPalMessageView] component.
	 * It makes use of the existing values for the message content and style
	 */
	private fun updateMessageUi() {
		messageContent?.let { content ->
			val builder = SpannableStringBuilder(content)
			// Set the content for calculating the line height
			val lineHeight: Int = messageTextView.lineHeight
			// Apply logo style
			messageLogoTag?.let { tag ->
				if (builder.contains(tag)) {
					builder.setupMessageLogo(logo.getAsset(color), tag, lineHeight)
				}
			}
			// Apply disclaimer style
			messageDisclaimer?.let { builder.setupDisclaimer(color, it) }
			// Apply everything to the text view
			messageTextView.apply {
				visibility = View.VISIBLE
				setTextColor(ContextCompat.getColor(context, color.colorResId))
				gravity = when (textAlign) {
					Align.LEFT -> Gravity.START
					Align.CENTER -> Gravity.CENTER_HORIZONTAL
					Align.RIGHT -> Gravity.END
				}
				text = builder
			}
		}
	}

	/**
	 * This function will update the local config related values based on what is provided from the [PayPalMessageView] xml custom view.
	 */
	private fun updateFromAttributes(typedArray: TypedArray) {
		/**
		 * DATA
		 */
		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_client_id)) {
			clientID = typedArray.getString(R.styleable.PayPalMessageView_paypal_client_id).toString()
			// throw error here if clientID is empty
			// PayPalErrors.InvalidClientIdException()
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_amount)) {
			amount = try {
				typedArray.getFloatOrThrow(R.styleable.PayPalMessageView_paypal_amount).toDouble()
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing amount attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_page_type)) {
			pageType = try {
				PageType(typedArray.getIntOrThrow(R.styleable.PayPalMessageView_paypal_page_type))
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing page_type attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_offer_type)) {
			offerType = try {
				OfferType(typedArray.getIntOrThrow(R.styleable.PayPalMessageView_paypal_offer_type))
			}
			catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing offer_type attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_buyer_country)) {
			buyerCountry = typedArray.getString(R.styleable.PayPalMessageView_paypal_buyer_country)
		}

		/**
		 * STYLE
		 */
		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_text_color)) {
			color = Color(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_text_color,
					Color.BLACK.value,
				),
			)
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_logo_type)) {
			logoType = LogoType(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_logo_type,
					LogoType.PRIMARY.value,
				),
			)
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_text_align)) {
			textAlign = Align(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_text_align,
					Align.LEFT.value,
				),
			)
		}
	}

	/**
	 * This function updates message content uses [Api.getMessageWithHash] to fetch the data.
	 */
	private fun updateMessageContent() {
		LogCat.debug(TAG, "updateMessageContent config: ${getConfig()}")
		if (!updateInProgress) {
			// Call OnLoading callback and prepare view for the process
			onLoading.invoke()
			messageTextView.visibility = View.GONE
			updateInProgress = true
			LogCat.debug(TAG, "Firing request to get message")

			requestDuration = measureTimeMillis {
				Api.getMessageWithHash(
					context,
					getConfig(),
					this.instanceId,
					this,
				)
			}.toInt()
		}
	}

	override fun onActionCompleted(result: ApiResult) {
		when (result) {
			is ApiResult.Success<*> -> {
				LogCat.debug(TAG, "onActionCompleted Success")
				val renderDuration = measureTimeMillis {
					this.onSuccess.invoke()
					this.messageDataResponse = result.response as ApiMessageData.Response
					updateContentValues(result.response)
					updateMessageUi()
				}.toInt()

				// Log that we successfully rendered the message
				logEvent(
					TrackingEvent(
						eventType = EventType.MESSAGE_RENDER,
						renderDuration,
						requestDuration,
					),
				)
			}

			is ApiResult.Failure<*> -> {
				LogCat.debug(TAG, "onActionCompleted Failure")
				// If we encountered a failure, we expect an exception to be returned.
				result.error?.let { this.onError(it) }
			}
		}
		updateInProgress = false
	}

	/**
	 * This function updates local values related to the message content
	 * @param response the response obtained from the message content fetch process
	 */
	private fun updateContentValues(response: ApiMessageData.Response) {
		messageContent = formatMessageContent(response, logoType)
		messageLogoTag = response.meta?.variables?.logoPlaceholder
		messageDisclaimer = response.content?.default?.disclaimer
		logo = Logo(logoType, response.meta?.creditProductGroup)
		messageTextView.setOnClickListener {
			onClick.invoke()
			// Log Message Click
			logEvent(
				TrackingEvent(
					eventType = EventType.MESSAGE_CLICK,
					pageViewLinkName = "banner_wrapper",
					pageViewLinkSource = "message",
				),
			)
			showWebView(response)
		}
	}

	/**
	 * Formats the message content based on the [ApiMessageData.Response] and [LogoType]
	 * The formatted message would depend on the values provided by the response and will later be used as the content of the [PayPalMessageView] component
	 */
	private fun formatMessageContent(
		response: ApiMessageData.Response,
		logoType: LogoType,
	): String {
		val builder = StringBuilder()
		val messageContent = response.content?.default?.main
		val logoTag = response.meta?.variables?.logoPlaceholder
		val disclaimer = response.content?.default?.disclaimer

		// Append message content if it exists
		messageContent?.let { content ->
			// Append LogoTag if logotype is PRIMARY or ALTERNATIVE and tag is not present
			logoTag?.let { tag ->
				if (logoType in listOf(
						LogoType.PRIMARY,
						LogoType.ALTERNATIVE,
					) && !content.contains(tag)
				) {
					builder.append("$tag ")
				}
			}
			// Append main message content
			builder.append(content)
		}

		// Append disclaimer if it exists
		disclaimer?.let { builder.append(" $it") }
		return builder.toString()
	}

	/**
	 * This function setup the [Logo] used as part of the [PayPalMessageView] component content.
	 * The asset to use and how to locate it will depend on the provided information.
	 * @param logoAsset the asset to use as part of the component. It can be an image or a string.
	 * @param logoTag the logo placeholder provided as part of the [ApiMessageData.Response]
	 * @param lineHeight the textview line height use for resizing the image logo assets
	 */
	private fun SpannableStringBuilder.setupMessageLogo(
		logoAsset: LogoAsset,
		logoTag: String,
		lineHeight: Int,
	) {
		val logoIndex = indexOf(logoTag)
		when (logoAsset) {
			is LogoAsset.StringAsset -> {
				val logoString = context.resources.getString(logoAsset.resId)
				replace(logoIndex, logoIndex + logoTag.length, logoString)
				setSpan(
					StyleSpan(android.graphics.Typeface.BOLD),
					logoIndex,
					logoIndex + logoString.length,
					Spannable.SPAN_INCLUSIVE_INCLUSIVE,
				)
			}

			is LogoAsset.ImageAsset -> {
				ContextCompat.getDrawable(context, logoAsset.resId)?.let { logoDrawable ->
					val logoHeight: Int
					val top: Int

					when {
						// Inline
						logoDrawable.intrinsicHeight > 200 && logoDrawable.intrinsicWidth > 200 -> {
							logoHeight = lineHeight
							top = 6
						}
						// Alternative
						logoDrawable.intrinsicHeight > 200 && logoDrawable.intrinsicWidth < 200 -> {
							logoHeight = lineHeight + 8
							top = 8
						}
						// Primary
						else -> {
							logoHeight = lineHeight + 4
							top = 4
						}
					}

					val width =
						(logoHeight - top) * logoDrawable.intrinsicWidth / logoDrawable.intrinsicHeight
					logoDrawable.setBounds(0, top, width, logoHeight)
					val alignCenter = 2

					setSpan(
						ImageSpan(logoDrawable, alignCenter),
						logoIndex,
						logoIndex + logoTag.length,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
					)
				}
			}
		}
	}

	/**
	 * This function setups the disclaimer used as part of the [PayPalMessageView] component content.
	 * @param color the current [Color] that will be used to format the disclaimer text style
	 * @param disclaimer the disclaimer text provided as part of the [ApiMessageData.Response]
	 */
	private fun SpannableStringBuilder.setupDisclaimer(
		color: Color,
		disclaimer: String,
	) {
		val disclaimerIndex = indexOf(disclaimer)

		if (color === Color.BLACK) {
			setSpan(
				ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue_600)),
				disclaimerIndex,
				disclaimerIndex + disclaimer.length,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE,
			)
		}
		setSpan(
			UnderlineSpan(),
			disclaimerIndex,
			disclaimerIndex + disclaimer.length,
			Spannable.SPAN_INCLUSIVE_INCLUSIVE,
		)
	}

	private fun logEvent(event: TrackingEvent) {
		// Build component Information
		val component = TrackingComponent(
			offerType = this.offerType,
			amount = this.amount.toString(),
			pageType = this.pageType,
			buyerCountryCode = this.buyerCountry,
			styleLogoType = this.logoType,
			styleColor = this.color,
			styleTextAlign = this.textAlign,
			messageType = this.messageDataResponse?.meta?.messageType,
			fdata = this.messageDataResponse?.meta?.fdata,
			debugId = this.messageDataResponse?.meta?.debugId,
			creditProductIdentifiers = this.messageDataResponse?.meta?.creditProductIdentifiers as MutableList<String>?,
			offerCountryCode = this.messageDataResponse?.meta?.offerCountryCode,
			merchantCountryCode = this.messageDataResponse?.meta?.merchantCountryCode,
			type = ComponentType.MESSAGE.toString(),
			instanceId = this.instanceId.toString(),
			originatingInstanceId = Api.originatingInstanceId.toString(),
			sessionId = Api.sessionId.toString(),
			componentEvents = mutableListOf(event),
		)

		Logger.getInstance(clientId = clientID).log(context, component)
	}
}
