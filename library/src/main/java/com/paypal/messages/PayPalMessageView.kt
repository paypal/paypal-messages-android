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
import androidx.core.content.ContextCompat
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.use
import com.paypal.messages.analytics.AnalyticsComponent
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.analytics.AnalyticsLogger
import com.paypal.messages.analytics.ComponentType
import com.paypal.messages.analytics.EventType
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalLanguage
import com.paypal.messages.config.PayPalLocale
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.data.PayPalMessageDataCallback
import com.paypal.messages.data.PayPalMessageDataProvider
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
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
import com.paypal.messages.config.message.style.PayPalMessageAlignment as Alignment
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
) : FrameLayout(context, attributeSet, defStyleAttr), PayPalMessageDataCallback {
	private val TAG = "PayPalMessage"
	private var messageTextView: TextView
	private var instanceId = UUID.randomUUID()
	private val dataProvider = PayPalMessageDataProvider()
	private var clickHandler: com.paypal.messages.data.PayPalMessageClickHandler? = null
	private var isClickListenerSet = false

	// Message Content
	private var logo = Logo()
	private var messageContent: String? = null
	private var messageDisclaimer: String? = null
	private var messageLogoTag: String? = null
	private var messageDataResponse: ApiMessageData.Response? = null
	private var requestDuration: Int? = null

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
				language = this.language,
				locale = this.locale,
				environment = this.environment ?: PayPalEnvironment.SANDBOX,
			),
			style = MessageStyle(this.color, this.logoType, this.textAlignment),
			viewStateCallbacks = ViewStateCallbacks(this.onLoading, this.onSuccess, this.onError),
			eventsCallbacks = EventsCallbacks(this.onClick, this.onApply),
		)
	}

	fun setConfig(config: MessageConfig) {
		clientID = config.data.clientID
		merchantID = config.data.merchantID
		partnerAttributionID = config.data.partnerAttributionID
		amount = config.data.amount
		buyerCountry = config.data.buyerCountry
		offerType = config.data.offerType
		pageType = config.data.pageType
		language = config.data.language
		locale = config.data.locale
		color = config.style.color
		logoType = config.style.logoType
		textAlignment = config.style.textAlignment
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
				debounceUpdateContent(Unit)
			}
		}
	var buyerCountry: String? = config.data.buyerCountry
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var language: PayPalLanguage? = config.data.language
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var locale: PayPalLocale? = config.data.locale
		set(arg) {
			if (field != arg) {
				field = arg
				debounceUpdateContent(Unit)
			}
		}
	var offerType: OfferType? = config.data.offerType
		set(arg) {
			if (field != arg) {
				field = arg
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
				Api.env = arg ?: PayPalEnvironment.SANDBOX
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
	var textAlignment: Alignment = config.style.textAlignment
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

	init {
		LayoutInflater.from(context).inflate(R.layout.paypal_message_view, this, true)

		messageTextView = findViewById(R.id.content)

		context.obtainStyledAttributes(attributeSet, R.styleable.PayPalMessageView).use { typedArray ->
			updateFromAttributes(typedArray)
		}
		if (config.data.clientID === "") LogCat.error(TAG, "ClientID is an empty string")
		updateMessageContent()
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		// Clean up click handler which will dismiss any modals
		clickHandler?.onCleanup()
		clickHandler = null
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

			// Apply bold styling for %bold% delimiters
			builder.applyBoldDelimiters("%bold%")

			// Apply disclaimer style
			messageDisclaimer?.let { builder.setupDisclaimer(color, it) }
			// TextView has textAlignment so this prevents clashing variables
			val payPalMessageViewTextAlignment = textAlignment
			// Apply everything to the text view
			messageTextView.apply {
				visibility = View.VISIBLE
				setTextColor(ContextCompat.getColor(context, color.colorResId))
				gravity = when (payPalMessageViewTextAlignment) {
					Alignment.LEFT -> Gravity.START
					Alignment.CENTER -> Gravity.CENTER_HORIZONTAL
					Alignment.RIGHT -> Gravity.END
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
			textAlignment = Alignment(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_text_align,
					Alignment.LEFT.value,
				),
			)
		}
	}

	/**
	 * This function updates message content using the PayPalMessageDataProvider.
	 */
	private fun updateMessageContent() {
		LogCat.debug(TAG, "Firing request to get message with config: ${getConfig()}")
		dataProvider.fetchMessageData(
			context,
			getConfig(),
			this.instanceId,
			this,
		)
	}

	override fun onLoading() {
		// Explicitly use 'this.' to reference the property, not this method
		this.onLoading.invoke()
	}

	override fun onSuccess(response: ApiMessageData.Response, duration: Int) {
		LogCat.debug(TAG, "onSuccess")
		val renderDuration = measureTimeMillis {
			this.onSuccess.invoke()
			this.messageDataResponse = response
			this.requestDuration = duration
			updateContentValues(response)
			updateMessageUi()
		}.toInt()

		// Log that we successfully rendered the message
		logEvent(
			AnalyticsEvent(
				eventType = EventType.MESSAGE_RENDERED,
				renderDuration = renderDuration.toString(),
				requestDuration = duration.toString(),
			),
		)
	}

	override fun onError(error: PayPalErrors.Base) {
		LogCat.debug(TAG, "onError: ${error.message}")
		// Invoke the user-provided error callback
		// Explicitly use 'this.' to reference the property, not this method
		this.onError.invoke(error)
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

		// Always recreate the click handler to ensure freshness
		clickHandler?.onCleanup()
		clickHandler = dataProvider.createClickHandler(
			context,
			getConfig(),
			instanceId,
		) { event -> logEvent(event) }

		// Set click listener only once
		if (!isClickListenerSet) {
			messageTextView.setOnClickListener {
				android.util.Log.d("PayPalMessage", "Message text view clicked, forwarding to click handler")
				try {
					val clickId = UUID.randomUUID().toString().substring(0, 8)
					android.util.Log.d("PayPalMessage", "[$clickId] Processing click...")
					if (clickHandler == null) {
						android.util.Log.d("PayPalMessage", "[$clickId] Recreating click handler...")
						clickHandler = dataProvider.createClickHandler(
							context,
							getConfig(),
							instanceId,
						) { event -> logEvent(event) }
					}
					android.util.Log.d("PayPalMessage", "[$clickId] Calling onMessageClick with response data available: ${response != null}")
					val handler = clickHandler
					if (handler != null) {
						handler.onMessageClick(
							response,
							onClick,
							onApply,
							onError,
						)
					} else {
						android.util.Log.e("PayPalMessage", "[$clickId] Click handler is null - creating emergency handler")
						val emergencyHandler = dataProvider.createClickHandler(
							context,
							getConfig(),
							instanceId,
						) { event -> logEvent(event) }
						emergencyHandler.onMessageClick(response, onClick, onApply, onError)
					}
					android.util.Log.d("PayPalMessage", "[$clickId] Click processed successfully")
				} catch (e: Exception) {
					android.util.Log.e("PayPalMessage", "Error in click handler: ${e.message}", e)
					onError.invoke(com.paypal.messages.utils.PayPalErrors.ModalFailedToLoad("Failed to show modal: ${e.message}", null))
				}
			}
			isClickListenerSet = true
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
		val mainContent = response.content?.default?.main
		val logoTag = response.meta?.variables?.logoPlaceholder
		val disclaimer = response.content?.default?.disclaimer

		val productGroup = response.meta?.creditProductGroup
		val brandingText = if (productGroup == ProductGroup.PAYPAL_CREDIT) "PayPal Credit" else "PayPal"

		val alternativeText = response.content?.default?.mainAlternative
			?: response.content?.generic?.mainAlternative
			?: logoTag?.let { mainContent?.replace(it, brandingText) ?: "" }
		val leadText = if (mainContent?.contains("$logoTag") == false) "$brandingText -" else ""
		val accessibilityText = "$leadText $alternativeText $disclaimer".trim()
		messageTextView.contentDescription = accessibilityText

		// Append message content if it exists
		mainContent?.let { content ->
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

	// Extension function to apply bold styling for substrings between custom delimiters
	private fun SpannableStringBuilder.applyBoldDelimiters(delimiter: String) {
		var start = this.toString().indexOf(delimiter)
		while (start != -1) {
			val end = this.toString().indexOf(delimiter, start + delimiter.length)
			if (end == -1) break
			val boldStart = start
			val boldEnd = end
			// Remove delimiters and get the substring
			val substring = this.substring(boldStart + delimiter.length, boldEnd)
			// Replace the whole %bold%My Substring%bold% with just My Substring
			replace(boldStart, boldEnd + delimiter.length, substring)
			// Apply bold span
			setSpan(
				StyleSpan(android.graphics.Typeface.BOLD),
				boldStart,
				boldStart + substring.length,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE,
			)
			// Search for next occurrence
			start = this.toString().indexOf(delimiter, boldStart + substring.length)
		}
	}

	private fun logEvent(event: AnalyticsEvent) {
		// Build component Information
		val component = AnalyticsComponent(
			offerType = this.offerType,
			amount = this.amount.toString(),
			pageType = this.pageType,
			buyerCountryCode = this.buyerCountry,
			language = this.language?.code,
			locale = this.locale?.code,
			styleLogoType = this.logoType,
			styleColor = this.color,
			styleTextAlign = this.textAlignment,
			messageType = this.messageDataResponse?.meta?.messageType,
			fdata = this.messageDataResponse?.meta?.fdata,
			debugId = this.messageDataResponse?.meta?.debugId,
			creditProductIdentifiers = this.messageDataResponse?.meta?.creditProductIdentifiers as MutableList<String>?,
			offerCountryCode = this.messageDataResponse?.meta?.offerCountryCode,
			merchantCountryCode = this.messageDataResponse?.meta?.merchantCountryCode,
			languageRendered = this.messageDataResponse?.meta?.language ?: "undefined",
			type = ComponentType.MESSAGE.toString(),
			instanceId = this.instanceId.toString(),
			originatingInstanceId = Api.originatingInstanceId.toString(),
			componentEvents = mutableListOf(event),
		)

		AnalyticsLogger.getInstance(clientId = clientID).log(context, component)
	}
}
