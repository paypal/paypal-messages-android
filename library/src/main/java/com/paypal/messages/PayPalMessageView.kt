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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.use
import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.errors.BaseException
import com.paypal.messages.io.Action
import com.paypal.messages.io.ActionResponse
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.logger.ComponentType
import com.paypal.messages.logger.EventType
import com.paypal.messages.logger.Logger
import com.paypal.messages.logger.TrackingComponent
import com.paypal.messages.logger.TrackingEvent
import com.paypal.messages.utils.LogCat
import java.util.UUID
import kotlin.system.measureTimeMillis
import com.paypal.messages.config.PayPalMessageOfferType as OfferType
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig
import com.paypal.messages.config.message.PayPalMessageData as MessageData
import com.paypal.messages.config.message.PayPalMessageEvents as MessageEvents
import com.paypal.messages.config.message.PayPalMessageStyle as MessageStyle
import com.paypal.messages.config.message.PayPalMessageViewState as MessageViewState
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
	config: MessageConfig? = null,
) : LinearLayout(context, attributeSet, defStyleAttr), OnActionCompleted {
	private val TAG = "PayPalMessage"
	private var messageTextView: TextView

	// Config values
	private var clientId: String = config?.data?.clientId ?: ""
	private var updateInProgress = false
	private var amount: Double? = null
	private var placement: String? = null
	private var offerType: OfferType? = null
	private var buyerCountry: String? = null
	private var currencyCode: CurrencyCode? = null
	var color: Color = Color.BLACK
		private set
	private var logoType: LogoType = LogoType.PRIMARY
	private var alignment: Align = Align.LEFT

	private var instanceId = UUID.randomUUID()

	// Full Message Data
	private var data: ActionResponse? = null

	// Message Content
	private var logo = Logo()
	private var messageContent: String? = null
	private var messageDisclaimer: String? = null
	private var messageLogoTag: String? = null

	// Modal Instance
	private var modal: ModalFragment? = null

	// Stats
	private var requestDuration: Int? = null

	/**
	 * Updates the onLoading callback used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will update the current onLoading callback used during the message content update operation
	 */
	private var onLoading: () -> Unit = {}

	/**
	 * Updates the onSuccess callback used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will update the current onSuccess callback used during the message content update operation
	 */
	private var onSuccess: () -> Unit = {}

	/**
	 * Updates the onError callback used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will update the current onError callback used during the message content update operation
	 */
	var onError: (error: BaseException) -> Unit = {}

	/**
	 * Updates the onClick callback used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will update the current onClick callback used for interaction with the [PayPalMessageView] component
	 */
	private var onClick: () -> Unit = {}

	/**
	 * Updates the onApply callback used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will update the current onApply callback used for interaction with the [PayPalMessageView] component
	 */
	private var onApply: () -> Unit = {}

	init {
		LayoutInflater.from(context).inflate(R.layout.paypal_message_view, this, true)

		messageTextView = findViewById(R.id.content)

		context.obtainStyledAttributes(attributeSet, R.styleable.PayPalMessageView).use { typedArray ->
			updateFromAttributes(typedArray)
		}
		config?.let { updateFromConfig(it) }
		updateMessageContent()
	}

	/**
	 * Updates the configuration associated with the [PayPalMessageView] component
	 *
	 * This function will also trigger an update in the component to get the new content based on the provided configuration
	 */
	fun setConfig(config: MessageConfig?) {
		updateFromConfig(config)
		updateMessageContent()
		LogCat.debug(TAG, "Was Message Configured?") // TODO Remove
	}

	/**
	 * ///////////////////////////////////
	 * COMPLETE CONFIG CATEGORIES SETTERS
	 * //////////////////////////////////
	 */

	fun setClientId(clientId: String) {
		this.clientId = clientId
	}

	/**
	 * Update the [MessageConfig] data currently associated with the [PayPalMessageView] component
	 *
	 * This function will also trigger an update in the component to get the new content based on the provided configuration
	 */
	fun setData(data: MessageData?) {
		amount = data?.amount
		placement = data?.placement
		offerType = data?.offerType
		buyerCountry = data?.buyerCountry
		currencyCode = data?.currencyCode

		// Update modal properties if it exists
		if (modal != null) {
			amount?.let { modal?.setAmount(it) }
//      offerType?.let { modal?.setOfferType(it) }
			buyerCountry?.let { modal?.setBuyerCountry(it) }
			currencyCode?.let { modal?.setCurrency(it.toString()) }
		}

		updateMessageContent()
	}

	/**
	 * Update the [MessageConfig] event callbacks currently associated with the [PayPalMessageView] component
	 *
	 * This function will also trigger an update in the component to get the new content based on the provided configuration
	 */
	fun setActionEventCallbacks(events: MessageEvents) {
		onClick = events.onClick
		onApply = events.onApply
	}

	/**
	 * Update the [MessageConfig] view state callbacks currently associated with the [PayPalMessageView] component
	 *
	 * This function will also trigger an update in the component to get the new content based on the provided configuration
	 */
	fun setViewStateCallbacks(viewState: MessageViewState) {
		onSuccess = viewState.onSuccess
		onError = viewState.onError
		onLoading = viewState.onLoading
	}

	/**
	 * Update the [MessageConfig] style currently associated with the [PayPalMessageView] component
	 *
	 * This function will also trigger an update in the component to get the new content based on the provided configuration
	 */
	fun setStyle(style: MessageStyle?) {
		color = style?.color ?: Color.BLACK
		alignment = style?.textAlign ?: Align.LEFT
		logoType = style?.logoType ?: LogoType.PRIMARY
		updateMessageContent()
	}

	/**
	 * ////////////////////////////
	 * SINGLE CONFIG VALUE SETTERS
	 * ////////////////////////////
	 */

	/**
	 * Updates the amount used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will also trigger an update in the component content to reflect the changes with the [MessageConfig] updated amount
	 */
	fun setAmount(amount: Double?) {
		if (this.amount != amount) {
			this.amount = amount
			// Update modal properties if it exists
			if (amount != null) {
				modal?.setAmount(amount)
			}
			updateMessageContent()
		}
	}

	/**
	 * Updates the placement used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will also trigger an update in the component content to reflect the changes with the [MessageConfig] updated placement
	 */
	fun setPlacement(placement: String?) {
		if (this.placement != placement) {
			this.placement = placement
			updateMessageContent()
		}
	}

	/**
	 * Updates the offerType used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will also trigger an update in the component content to reflect the changes with the [MessageConfig] updated offerType
	 */
	fun setOfferType(offerType: OfferType?) {
		if (this.offerType != offerType) {
			this.offerType = offerType
			// Update modal properties if it exists
//                if (offerType != null) {
//                    modal?.setOfferType(offerType)
//                }
			updateMessageContent()
		}
	}

	/**
	 * Updates the buyerCountry used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will also trigger an update in the component content to reflect the changes with the [MessageConfig] updated buyerCountry
	 */
	fun setBuyerCountry(buyerCountry: String?) {
		if (this.buyerCountry != buyerCountry) {
			this.buyerCountry = buyerCountry
			// Update modal properties if it exists
			if (buyerCountry != null) {
				modal?.setBuyerCountry(buyerCountry)
			}
			updateMessageContent()
		}
	}

	/**
	 * Updates the color used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will change the component current style to reflect the changes with the provided color
	 */
	fun setColor(color: Color?) {
		if (this.color != color) {
			this.color = color ?: Color.BLACK
			updateMessageUi()
		}
	}

	/**
	 * Updates the logotype used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will also trigger an update in the component content to reflect the changes with the provided logotype
	 */
	fun setLogoType(logoType: LogoType?) {
		if (this.logoType != logoType) {
			this.logoType = logoType ?: LogoType.PRIMARY
			updateMessageContent()
		}
	}

	/**
	 * Updates the color used in [MessageConfig] for the current [PayPalMessageView].
	 *
	 * This function will change the component current style to reflect the changes with the provided text alignment
	 */
	fun setTextAlignment(alignment: Align?) {
		if (this.alignment != alignment) {
			this.alignment = alignment ?: Align.LEFT
			updateMessageUi()
		}
	}

	private fun showWebView(response: ActionResponse) {
		val modal = modal ?: run {
			val modal = ModalFragment(clientId)
			// Build modal config
			val modalConfig = ModalConfig(
				amount = amount,
				currency = currencyCode.toString(),
				buyerCountry = buyerCountry,
				offer = offerType,
				ignoreCache = false,
				devTouchpoint = false,
				stageTag = null,
				events = ModalEvents(
					onApply = onApply,
					onClick = onClick,
					onError = onError,
				),
				modalCloseButton = this.data?.meta?.modalCloseButton!!,
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

	fun refresh() {
		updateMessageContent()
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
			// Apply the everything to the text view
			messageTextView.apply {
				visibility = View.VISIBLE
				setTextColor(ContextCompat.getColor(context, color.colorResId))
				gravity = when (alignment) {
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
			clientId = typedArray.getString(R.styleable.PayPalMessageView_paypal_client_id).toString()
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_amount)) {
			amount = try {
				typedArray.getFloatOrThrow(R.styleable.PayPalMessageView_paypal_amount).toDouble()
			} catch (ex: Exception) {
				LogCat.error(TAG, "Error parsing amount attribute")
				null
			}
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_placement)) {
			placement = typedArray.getString(R.styleable.PayPalMessageView_paypal_placement)
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_offer_type)) {
			offerType = try {
				OfferType(typedArray.getIntOrThrow(R.styleable.PayPalMessageView_paypal_offer_type))
			} catch (ex: Exception) {
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
			color = PayPalMessageColor(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_text_color,
					Color.BLACK.value,
				),
			)
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_logo_type)) {
			logoType = PayPalMessageLogoType(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_logo_type,
					LogoType.PRIMARY.value,
				),
			)
		}

		if (typedArray.hasValue(R.styleable.PayPalMessageView_paypal_text_align)) {
			alignment = PayPalMessageAlign(
				typedArray.getInt(
					R.styleable.PayPalMessageView_paypal_text_align,
					Align.LEFT.value,
				),
			)
		}
	}

	/**
	 * This function will update the local config related values based on the provided [MessageConfig].
	 */
	private fun updateFromConfig(config: MessageConfig?) {
		LogCat.debug(TAG, "updateFromConfig:\n$config")
		clientId = config?.data?.clientId ?: ""
		amount = config?.data?.amount
		placement = config?.data?.placement
		offerType = config?.data?.offerType
		buyerCountry = config?.data?.buyerCountry
		currencyCode = config?.data?.currencyCode
		color = config?.style?.color ?: Color.BLACK
		alignment = config?.style?.textAlign ?: Align.LEFT
		logoType = config?.style?.logoType ?: LogoType.PRIMARY
		onError = config?.viewState?.onError ?: {}
		onSuccess = config?.viewState?.onSuccess ?: {}
		onLoading = config?.viewState?.onLoading ?: {}
		onApply = config?.events?.onApply ?: {}
		onClick = config?.events?.onClick ?: {}
	}

	fun setViewStates(viewState: MessageViewState) {
		onLoading = viewState.onLoading
		onError = viewState.onError
		onSuccess = viewState.onSuccess
	}

	/**
	 * //////////////////////
	 * UPDATE MESSAGE CONTENT
	 * /////////////////////
	 */

	/**
	 * This function updates the message content making use of the [Action] to fetch the data.
	 */
	private fun updateMessageContent() {
		if (!updateInProgress) {
			// Call OnLoading callback and prepare view for the process
			onLoading.invoke()
			messageTextView.visibility = View.GONE
			updateInProgress = true
			LogCat.debug(TAG, "Firing request to get message")

			val action = Action(context = context)

			requestDuration = measureTimeMillis {
				action.execute(
					MessageConfig(
						MessageData(clientId, amount, placement, offerType, buyerCountry, currencyCode),
						MessageStyle(logoType, color, alignment),
					),
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
					onSuccess.invoke()
					this.data = result.response as ActionResponse
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
				result.error?.let { onError(it) }
			}
		}
		updateInProgress = false
	}

	/**
	 * This function updates local values related to the message content
	 * @param response the response obtained from the message content fetch process
	 */
	private fun updateContentValues(response: ActionResponse) {
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
					linkName = "banner_wrapper",
					linkSrc = "message",
				),
			)
			showWebView(response)
		}
	}

	/**
	 * Formats the message content based on the [ActionResponse] and [LogoType]
	 * The formatted message would depend on the values provided by the response and will later be used as the content of the [PayPalMessageView] component
	 */
	private fun formatMessageContent(
		response: ActionResponse,
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
	 * @param logoTag the logo placeholder provided as part of the [ActionResponse]
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
					val width =
						lineHeight * logoDrawable.intrinsicWidth / logoDrawable.intrinsicHeight
					logoDrawable.setBounds(0, 0, width, lineHeight)
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
	 * @param disclaimer the disclaimer text provided as part of the [ActionResponse]
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
			placement = this.placement,
			buyerCountryCode = this.buyerCountry,
			styleLogoType = this.logoType,
			styleColor = this.color,
			styleTextAlign = this.alignment,
			messageType = this.data?.meta?.messageType,
			fdata = this.data?.meta?.fdata,
			debugId = this.data?.meta?.debug_id,
			creditProductIdentifiers = this.data?.meta?.creditProductIdentifiers as MutableList<String>?,
			offerCountryCode = this.data?.meta?.offerCountryCode,
			merchantCountryCode = this.data?.meta?.merchantCountryCode,
			type = ComponentType.MESSAGE.toString(),
			instanceId = this.instanceId.toString(),
			events = mutableListOf(event),
		)

		Logger.getInstance(clientId = clientId).log(context, component)
	}
}
