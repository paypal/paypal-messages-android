package com.paypal.messages

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.paypal.messages.config.Channel
import com.paypal.messages.config.CurrencyCode
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.extensions.dp
import com.paypal.messages.io.Api
import com.paypal.messages.logger.ComponentType
import com.paypal.messages.logger.EventType
import com.paypal.messages.logger.Logger
import com.paypal.messages.logger.TrackingComponent
import com.paypal.messages.logger.TrackingEvent
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.net.URI
import java.util.UUID
import kotlin.system.measureTimeMillis
import com.paypal.messages.config.PayPalMessageOfferType as OfferType

internal class ModalFragment constructor(
	private val clientId: String,
) : BottomSheetDialogFragment() {
	private val TAG = "PayPalMessageModal"
	private val offsetTop = 50.dp

	private var modalUrl: String? = null

	// MODAL CONFIG VALUES
	var amount: Double? = null
		set(amountArg) {
			if (field != amountArg) {
				field = amountArg
				setJsValue(name = "amount", value = amountArg)
			}
		}
	var buyerCountry: String? = null
		set(buyerCountryArg) {
			if (field != buyerCountryArg) {
				field = buyerCountryArg
				setJsValue(name = "buyerCountry", value = buyerCountry)
			}
		}
	private var channel: Channel = Channel.NATIVE
	var currencyCode: CurrencyCode? = null
		set(currencyArg) {
			if (field != currencyArg) {
				field = currencyArg
				setJsValue(name = "currency", value = currencyArg.toString())
			}
		}
	private var devTouchpoint: Boolean = true
	private var ignoreCache: Boolean = false
	var offerType: OfferType? = null
		set(offerArg) {
			if (field != offerArg) {
				field = offerArg
				setJsValue(name = "offer", value = offerArg.toString())
			}
		}
	private var stageTag: String? = null

	private var inErrorState: Boolean = false
	private var currentUrl: String? = null
	private var webView: WebView? = null
	private var rootView: View? = null
	private var dialog: BottomSheetDialog? = null
	private var closeButtonData: ModalCloseButton? = null
	private var instanceId = UUID.randomUUID()

	private fun <T> setJsValue(name: String, value: T) {
		LogCat.debug(TAG, "$name changed. Calling actions.updateProps({'$name':'$value'})")
		this.webView?.evaluateJavascript("javascript:actions.updateProps({'$name':'$value'});", null)
	}

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreateView(
		inflator: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		val rootView =
			inflator.inflate(R.layout.paypal_message_modal_sheet_layout, container, false)
		val closeButton = rootView.findViewById<ImageButton>(R.id.ModalCloseButton)

		closeButton.layoutParams.height = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			this.closeButtonData?.height!!.toFloat(), resources.displayMetrics,
		).toInt()
		closeButton.layoutParams.width = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			this.closeButtonData?.width!!.toFloat(), resources.displayMetrics,
		).toInt()

		val colorInt = Color.parseColor(this.closeButtonData?.color)
		closeButton.background.colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP)

		closeButton?.setOnClickListener {
			logEvent(TrackingEvent(eventType = EventType.MODAL_CLOSE))
			dialog?.hide()
		}

		// If we already have a WebView, don't reset it
		LogCat.debug(TAG, "Configuring WebView Settings and Handlers")
		val webView = rootView.findViewById<RoundedWebView>(R.id.ModalWebView)

		// Programmatically set bottom margin instead of in XML since we also apply it to the
		// dialog behavior below to control it in a single location. The expanded offset below shifts
		// the whole modal down, so we offset that by applying a margin to the bottom of the WebView
		// which keeps the scrollable space on screen
		webView.layoutParams = (webView.layoutParams as RelativeLayout.LayoutParams).apply {
			bottomMargin = offsetTop
		}

		webView.settings.javaScriptEnabled = true
		webView.settings.domStorageEnabled = true
		webView.settings.allowContentAccess
		webView.addJavascriptInterface(this, "Android")
		// For Debugging
		webView.webChromeClient = object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
				val source = "${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"
				LogCat.debug(TAG, "$source: ${consoleMessage.message()}")
				return super.onConsoleMessage(consoleMessage)
			}
		}

		// Configure WebView
		webView.webViewClient = object : WebViewClient() {
			// TODO remove for production
			@SuppressLint("WebViewClientOnReceivedSslError")
			override fun onReceivedSslError(
				view: WebView,
				handler: SslErrorHandler,
				error: SslError,
			) {
				LogCat.debug(TAG, "Bypassing SSL check")
				handler.proceed()
			}

			override fun shouldOverrideUrlLoading(
				view: WebView?,
				request: WebResourceRequest?,
			): Boolean {
				val currentUri = URI.create(currentUrl)
				val currentHost = currentUri.host
				val currentPath = currentUri.path.split("/").getOrNull(1)
				val requestUri = request?.url ?: return false
				val requestHost = requestUri.host ?: return false
				val requestPath = requestUri.pathSegments.getOrNull(0) ?: return false

				return if (requestHost == currentHost && requestPath == currentPath) {
					false
				}
				else {
					val intent = Intent(Intent.ACTION_VIEW, requestUri)
					requireActivity().startActivity(intent)
					true
				}
			}

			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				handlePageFinished(url)
			}

			override fun onReceivedError(
				view: WebView?,
				request: WebResourceRequest?,
				error: WebResourceError?,
			) {
				super.onReceivedError(view, request, error)
				error?.let { e ->
					LogCat.debug(TAG, "Error Code ${e.errorCode}: ${e.description}")
				}

				val mainPageFailedToLoad = request?.url?.toString().equals(currentUrl)
				val urlType = if (mainPageFailedToLoad) "Main" else "Non-main"
				LogCat.debug(TAG, "$urlType URL failed: ${request?.url}")

				if (mainPageFailedToLoad) {
					handleError(error)
				}
			}

			override fun onReceivedHttpError(
				view: WebView?,
				request: WebResourceRequest?,
				errorResponse: WebResourceResponse?,
			) {
				// Can be  a 404 of any asset, so double check error is not on main content?
				super.onReceivedHttpError(view, request, errorResponse)
				errorResponse?.let { e ->
					LogCat.debug(TAG, "HTTP Error Code ${e.statusCode}: ${e.reasonPhrase}\nData: ${e.data}")
				}

				val mainPageFailedToLoad = request?.url?.toString().equals(currentUrl)
				val urlType = if (mainPageFailedToLoad) "Main" else "Non-main"
				LogCat.debug(TAG, "$urlType URL failed: ${request?.url}")

				if (mainPageFailedToLoad) {
					handleError(errorResponse)
				}
			}
		}

		// After configuring the WebView, set it as a global var
		this.webView = webView
		this.rootView = rootView
		return rootView
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialog)

		val dialog = (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
			window?.setBackgroundDrawableResource(android.R.color.transparent)
			behavior.isFitToContents = false
			behavior.expandedOffset = offsetTop
			behavior.isHideable = true
			behavior.isDraggable = true
			behavior.state = BottomSheetBehavior.STATE_EXPANDED
		}

		this.dialog = dialog

		return dialog
	}

	fun expand() {
		this.dialog?.show()
	}

	fun init(config: ModalConfig) {
		// Set Parameters for Modal
		this.amount = config.amount
		this.buyerCountry = config.buyerCountry
		this.channel = config.channel
		this.currencyCode = config.currencyCode
		this.devTouchpoint = config.devTouchpoint
		this.ignoreCache = config.ignoreCache
		this.offerType = config.offer
		this.stageTag = config.stageTag

		// Set Callbacks for Modal Actions
		this.onClick = config.events?.onClick ?: {}
		this.onLoading = config.events?.onLoading ?: {}
		this.onSuccess = config.events?.onSuccess ?: {}
		this.onError = config.events?.onError ?: {}
		this.onCalculate = config.events?.onCalculate ?: {}
		this.onShow = config.events?.onShow ?: {}
		this.onClose = config.events?.onClose ?: {}
		this.onApply = config.events?.onClose ?: {}

		// Set Configuration for Modal Close Button
		this.closeButtonData = config.modalCloseButton
	}

	// Handles showing the error screen when the browser errors
	fun handleError(error: WebResourceError?) {
		val errorName = PayPalErrors.ModalFailedToLoad::class.java.simpleName
		val errorDescription = "Browser Error ${error?.description}"
		LogCat.debug(TAG, "$errorName - $errorDescription")

		this.webView?.loadUrl("about:blank")
		openModalInBrowser()
		this.dismiss()
		inErrorState = true
		this.onError(PayPalErrors.ModalFailedToLoad(errorDescription))
		logEvent(
			TrackingEvent(
				eventType = EventType.MODAL_ERROR,
				errorName = errorName,
				errorDescription = errorDescription,
			),
		)
	}

	// Handles showing the error screen when there's an HTTP error
	fun handleError(error: WebResourceResponse?) {
		val errorName = PayPalErrors.ModalFailedToLoad::class.java.simpleName
		val errorDescription = "HTTP Error ${error?.statusCode}, ${error?.reasonPhrase}"
		LogCat.debug(TAG, "$errorName - $errorDescription")

		this.webView?.loadUrl("about:blank")
		openModalInBrowser()
		this.dismiss()
		inErrorState = true
		this.onError(PayPalErrors.ModalFailedToLoad(errorDescription))
		logEvent(
			TrackingEvent(
				eventType = EventType.MODAL_ERROR,
				errorName = errorName,
				errorDescription = errorDescription,
			),
		)
	}

	private fun openModalInBrowser() {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
		requireActivity().startActivity(intent)
	}

	fun handlePageFinished(url: String?) {
		LogCat.debug(TAG, "Modal loaded with url: $url")
		val progressBar = rootView?.findViewById<ProgressBar>(R.id.progress_bar)
		progressBar?.visibility = ProgressBar.INVISIBLE
		// Callback for onLoad
	}

	// This function is called when the modal is usually already instantiated
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		logEvent(TrackingEvent(eventType = EventType.MODAL_OPEN))
		this.onLoading()
		val url = Api.createModalUrl(clientId, amount, buyerCountry, offerType)

		LogCat.debug(TAG, "Start show process for modal with webView: $webView")
		val requestDuration = measureTimeMillis {
			if (inErrorState) {
				LogCat.debug(TAG, "Modal had error, resetting state and reloading WebView with URL: $url")
				inErrorState = false
			}
			else {
				if (modalUrl == null) {
					LogCat.debug(TAG, "Modal URL was null, new URL is: $url")
					modalUrl = url
				}
				else {
					LogCat.debug(TAG, "Modal reopened with original URL: $currentUrl")
				}
			}
			this.webView?.loadUrl(url)
			this.onShow()
		}.toInt()

		val renderDuration = measureTimeMillis {
			super.onViewCreated(view, savedInstanceState)
		}.toInt()

		logEvent(
			TrackingEvent(
				eventType = EventType.MODAL_RENDER,
				renderDuration,
				requestDuration,
			),
		)
	}

	/**
	 * This is called by library in the WebView to pass back various events and stats.
	 * See paypal-messaging-components/src/components/modal/v2/lib/zoid-polyfill.js for how it works.
	 */
	@JavascriptInterface
	fun paypalMessageModalCallbackHandler(passedParams: String) {
		val params = if (passedParams != "") passedParams else "{\"name\": \"\", \"args\": [{}]"
		val nameAndArgs = JsonParser.parseString(params).asJsonObject
		val name = nameAndArgs.get("name").asString
		val args = nameAndArgs.get("args").asJsonArray[0].asJsonObject
		LogCat.debug(TAG, "CallbackHandler:\n  name = $name\n  args = $args")

		// If __shared__ does not exist, use an empty object
		val sharedJson = args.get("__shared__") ?: JsonParser.parseString("{}")
		val shared = jsonElementToMutableMap(sharedJson)
		when (name) {
			"onClick" -> {
				val pageViewLinkName = args.get("page_view_link_name")?.asString
				val pageViewLinkSource = args.get("page_view_link_source")?.asString
				if (pageViewLinkName == "Apply Now") {
					this.onApply()
				}
				else {
					this.onClick()
				}
				logEvent(
					TrackingEvent(
						eventType = EventType.MODAL_CLICK,
						pageViewLinkSource = pageViewLinkSource,
						pageViewLinkName = pageViewLinkName,
					),
					shared,
				)
			}

			"onCalculate" -> {
				val calculatorAmount = args.get("amount")?.asString
				this.onCalculate()
				logEvent(
					TrackingEvent(
						eventType = EventType.MODAL_CLICK,
						data = "$calculatorAmount",
					),
					shared,
				)
			}

			else -> {
				// do something else or throw an exception
			}
		}
		// Parse and call correct callback
	}

	private fun jsonElementToMutableMap(jsonElement: JsonElement): MutableMap<String, Any> {
		val mutableMap = mutableMapOf<String, Any>()

		if (jsonElement.isJsonObject) {
			val jsonObject = jsonElement.asJsonObject
			for ((key, value) in jsonObject.entrySet()) {
				mutableMap[key] = jsonValueToAny(value)
			}
		}

		return mutableMap
	}

	private fun jsonValueToAny(jsonElement: JsonElement): Any {
		return when {
			jsonElement.isJsonPrimitive -> {
				val primitive = jsonElement.asJsonPrimitive
				when {
					primitive.isBoolean -> primitive.asBoolean
					primitive.isNumber -> primitive.asNumber
					else -> primitive.asString
				}
			}
			jsonElement.isJsonArray -> jsonElement.asJsonArray
			jsonElement.isJsonObject -> jsonElementToMutableMap(jsonElement)
			else -> throw IllegalArgumentException("Unsupported JSON element type: ${jsonElement::class.java.simpleName}")
		}
	}

	// Callbacks used in Modal
	var onLoading: () -> Unit = {}
	private var onSuccess: () -> Unit = {}
	var onError: (error: PayPalErrors.Base) -> Unit = {}
	var onApply: () -> Unit = {}

	// Grab from Message
	var onCalculate: () -> Unit = {}
	var onShow: () -> Unit = {}
	var onClick: () -> Unit = {}
	private var onClose: () -> Unit = {}

	private fun logEvent(event: TrackingEvent, dynamicKeys: MutableMap<String, Any>? = null) {
		val component = TrackingComponent(
			amount = this.amount.toString(),
			buyerCountryCode = this.buyerCountry,
			type = ComponentType.MODAL.toString(),
			instanceId = this.instanceId.toString(),
			componentEvents = mutableListOf(event),
			__shared__ = dynamicKeys,
		)

		context?.let { Logger.getInstance(clientId = clientId).log(it, component) }
	}
}
