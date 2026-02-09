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
import android.os.Message
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.paypal.messages.analytics.AnalyticsComponent
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.analytics.AnalyticsLogger
import com.paypal.messages.analytics.ComponentType
import com.paypal.messages.analytics.EventType
import com.paypal.messages.config.Channel
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.extensions.dp
import com.paypal.messages.extensions.jsonElementToMutableMap
import com.paypal.messages.io.Api
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.net.URI
import java.util.UUID
import kotlin.system.measureTimeMillis
import com.paypal.messages.config.PayPalMessageOfferType as OfferType

internal class ModalFragment : BottomSheetDialogFragment() {
	companion object {
		private const val ARG_CLIENT_ID = "clientId"

		/**
		 * Factory method to create a new instance of ModalFragment with clientId.
		 * This pattern is required for proper fragment state restoration when
		 * "Don't keep activities" is enabled.
		 */
		fun newInstance(clientId: String): ModalFragment {
			return ModalFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_CLIENT_ID, clientId)
				}
			}
		}
	}

	private val TAG = "PayPalMessageModal"
	private val offsetTop = 50.dp
	private val gson = GsonBuilder().setPrettyPrinting().create()

	/**
	 * Gets the clientId from arguments Bundle.
	 * This ensures proper state restoration when Android recreates the fragment.
	 */
	private val clientId: String
		get() = arguments?.getString(ARG_CLIENT_ID) ?: throw IllegalStateException("clientId must be set via newInstance()")

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
	private var devTouchpoint: Boolean = true
	private var ignoreCache: Boolean = false
	var offerType: OfferType? = null
		set(offerArg) {
			if (field != offerArg) {
				field = offerArg
				setJsValue(name = "offer", value = offerArg.toString())
			}
		}
	var language: String? = null
	var locale: String? = null
	private var stageTag: String? = null

	private var inErrorState: Boolean = false
	private var webView: WebView? = null
	private var rootView: View? = null
	private var dialog: BottomSheetDialog? = null
	private var closeButtonData: ModalCloseButton? = null
	private var instanceId = UUID.randomUUID()
	
	/**
	 * Sets up an external WebView with the modal content.
	 * This method can be used by Compose UI to initialize a WebView.
	 *
	 * @param webView The WebView to configure
	 */
	@SuppressLint("SetJavaScriptEnabled")
	fun setupWebView(webView: WebView) {
		this.webView = webView
		webView.settings.apply {
			javaScriptEnabled = true
			domStorageEnabled = true
			setSupportMultipleWindows(true)
			allowContentAccess = true
		}
		
		// Configure WebView client
		webView.webViewClient = createWebViewClient()
		webView.webChromeClient = createWebChromeClient()
		
		// Add JavaScript interface for communication
		webView.addJavascriptInterface(this, "Android")
		
		// Load the modal URL
		reloadUrl()
	}
	
	/**
	 * Creates a WebViewClient for handling page loading and error states
	 */
	private fun createWebViewClient(): WebViewClient {
		return object : WebViewClient() {
			// TODO remove for production
			@SuppressLint("WebViewClientOnReceivedSslError")
			override fun onReceivedSslError(v: WebView, handler: SslErrorHandler, e: SslError) {
				if (BuildConfig.IS_DEV) {
					LogCat.debug(TAG, "Bypassing SSL check")
					handler.proceed()
				} else {
					handler.cancel()
				}
			}

			override fun shouldOverrideUrlLoading(
				view: WebView?,
				request: WebResourceRequest?,
			): Boolean {
				val currentUri = modalUrl?.let { URI.create(it) }
				val currentHost = currentUri?.host
				val currentPath = currentUri?.path?.split("/")?.getOrNull(1)
				val requestUri = request?.url ?: return false
				val requestHost = requestUri.host ?: return false
				val requestPath = requestUri.pathSegments.getOrNull(0) ?: return false

				return if (requestHost == currentHost && requestPath == currentPath) {
					false
				} else {
					try {
						val intent = Intent(Intent.ACTION_VIEW, requestUri)
						view?.context?.startActivity(intent)
					} catch (e: Exception) {
						LogCat.error(TAG, "Failed to open URL: $requestUri")
					}
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

				val mainPageFailedToLoad = request?.url?.toString().equals(modalUrl)
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
				// Can be a 404 of any asset, so double check error is not on main content?
				super.onReceivedHttpError(view, request, errorResponse)
				errorResponse?.let { e ->
					LogCat.debug(TAG, "HTTP Error Code ${e.statusCode}: ${e.reasonPhrase}\nData: ${e.data}")
				}

				val mainPageFailedToLoad = request?.url?.toString().equals(modalUrl)
				val urlType = if (mainPageFailedToLoad) "Main" else "Non-main"
				LogCat.debug(TAG, "$urlType URL failed: ${request?.url}")

				if (mainPageFailedToLoad) {
					handleError(errorResponse)
				}
			}
		}
	}
	
	/**
	 * Creates a WebChromeClient for debugging and console logging
	 */
	private fun createWebChromeClient(): WebChromeClient {
		return object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
				val source = "${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"
				LogCat.debug(TAG, "\n$source:\n  ${consoleMessage.message()}\n")
				return super.onConsoleMessage(consoleMessage)
			}

			override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
				val transport = resultMsg?.obj as? WebView.WebViewTransport
				val tempWebView = WebView(view?.context ?: return false)

				tempWebView.webViewClient = object : WebViewClient() {
					override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
						val uri = request?.url ?: return false
						return try {
							val intent = Intent(Intent.ACTION_VIEW, uri)
							v?.context?.startActivity(intent)
							true
						} catch (e: Exception) {
							LogCat.error(TAG, "Failed to open new window URL: $uri")
							false
						}
					}
				}

				transport?.webView = tempWebView
				resultMsg?.sendToTarget()
				return true
			}
		}
	}
	
	/**
	 * Loads or reloads the URL for the modal
	 */
	private fun reloadUrl() {
		val url = Api.createModalUrl(clientId, amount, buyerCountry, offerType, language, locale)
		LogCat.debug(TAG, "Loading modal URL: $url")
		modalUrl = url
		webView?.loadUrl(url)
		onLoading()
		onShow()
	}

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
		
		// Use default values if closeButtonData is null (can happen during fragment recreation)
		// Default values match ModalCloseButton's init block defaults
		val buttonHeight = closeButtonData?.height ?: 26
		val buttonWidth = closeButtonData?.width ?: 26
		val buttonColor = closeButtonData?.color ?: "#001435"
		val buttonAltText = closeButtonData?.alternativeText ?: "PayPal learn more modal close"
		
		closeButton.contentDescription = buttonAltText

		closeButton.layoutParams.height = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			buttonHeight.toFloat(), resources.displayMetrics,
		).toInt()
		closeButton.layoutParams.width = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			buttonWidth.toFloat(), resources.displayMetrics,
		).toInt()

		val colorInt = Color.parseColor(buttonColor)
		closeButton.background.colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP)

		closeButton?.setOnClickListener {
			// Properly dismiss the fragment instead of just hiding the dialog
			// This ensures the fragment is removed from the fragment manager
			// and won't be restored when the activity is recreated
			dismiss()
		}

		// If we already have a WebView, don't reset it
		LogCat.debug(TAG, "Configuring WebView Settings and Handlers")
		val webView = rootView.findViewById<RoundedWebView>(R.id.ModalWebView)
		webView.apply {
			// Set the bottom margin here instead of in XML so it is controlled in one single location.
			// The offset shifts the modal down, so a bottom margin keeps the scrollable space on screen.
			(layoutParams as RelativeLayout.LayoutParams).apply { bottomMargin = offsetTop }

			// Show a white background while content loads to avoid black flash
			setBackgroundColor(Color.WHITE)
		}
		
		// Set up the WebView using our helper method
		setupWebView(webView)

		// After configuring the WebView, set the root view
		this.rootView = rootView
		return rootView
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		// Use no explicit style; configure window and shape programmatically
		setStyle(STYLE_NO_FRAME, 0)

		val dialog = (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
			window?.setBackgroundDrawableResource(android.R.color.transparent)
			behavior.isFitToContents = false
			behavior.expandedOffset = offsetTop
			behavior.isHideable = true
			behavior.isDraggable = false
			behavior.state = BottomSheetBehavior.STATE_EXPANDED

			// Add overlay dim behind the bottom sheet
			window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
			window?.setDimAmount(0.5f)

			// Allow tapping outside (overlay) to dismiss
			setCanceledOnTouchOutside(true)
		}

		// Ensure fragment is cancelable so overlay taps close the modal
		isCancelable = true

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
		this.devTouchpoint = config.devTouchpoint
		this.ignoreCache = config.ignoreCache
		this.offerType = config.offer
		this.language = config.language?.code
		this.locale = config.locale?.code
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
			AnalyticsEvent(
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
			AnalyticsEvent(
				eventType = EventType.MODAL_ERROR,
				errorName = errorName,
				errorDescription = errorDescription,
			),
		)
	}

	private fun openModalInBrowser() {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(modalUrl))
		requireActivity().startActivity(intent)
	}

	fun handlePageFinished(url: String?) {
		LogCat.debug(TAG, "Modal loaded with url: $url")
		val progressBar = rootView?.findViewById<ProgressBar>(R.id.progress_bar)
		progressBar?.visibility = ProgressBar.INVISIBLE
		// Keep white background; no change needed
		// Callback for onLoad
	}

	// This function is called when the modal is usually already instantiated
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		logEvent(AnalyticsEvent(eventType = EventType.MODAL_VIEWED))
		this.onLoading()
		val url = Api.createModalUrl(clientId, amount, buyerCountry, offerType, language, locale)

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
					LogCat.debug(TAG, "Modal reopened with original URL: $modalUrl")
				}
			}
			this.webView?.loadUrl(url)
			this.onShow()
		}.toInt()

		val renderDuration = measureTimeMillis {
			super.onViewCreated(view, savedInstanceState)
		}.toInt()

		logEvent(
			AnalyticsEvent(
				eventType = EventType.MODAL_RENDERED,
				renderDuration = renderDuration.toString(),
				requestDuration = requestDuration.toString(),
			),
		)
	}

	override fun onDismiss(dialog: android.content.DialogInterface) {
		super.onDismiss(dialog)
		// Call the onClose callback when the modal is dismissed
		onClose.invoke()
		LogCat.debug(TAG, "Modal dismissed, onClose callback invoked")
	}

	/**
	 * This is called by library in the WebView to pass back various events and stats.
	 * See paypal-messaging-components/src/components/modal/v2/lib/zoid-polyfill.js for how it works.
	 */
	@JavascriptInterface
	fun paypalMessageModalCallbackHandler(passedParams: String) {
		val params = if (passedParams != "") passedParams else """{"name": "", "args": [{}]} """
		val nameAndArgs = JsonParser.parseString(params).asJsonObject
		val name = nameAndArgs.get("name").asString
		val args = nameAndArgs.get("args").asJsonArray[0].asJsonObject
		LogCat.debug(TAG, "CallbackHandler:\n  name = $name\n  args = ${gson.toJson(args)}")

		// If __shared__ does not exist, use an empty object
		val sharedJson = args.get("__shared__") ?: JsonParser.parseString("{}")
		val shared = Gson::class.jsonElementToMutableMap(sharedJson)
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
					AnalyticsEvent(
						eventType = EventType.MODAL_CLICKED,
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
					AnalyticsEvent(
						eventType = EventType.MODAL_CLICKED,
						data = "$calculatorAmount",
					),
					shared,
				)
			}

			"onReady" -> {
				logEvent(
					AnalyticsEvent(
						eventType = EventType.MODAL_RENDERED,
						renderDuration = args.get("render_duration")?.asString ?: "",
						requestDuration = args.get("request_duration")?.asString ?: "",
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

	private fun logEvent(event: AnalyticsEvent, dynamicKeys: MutableMap<String, Any>? = null) {
		val component = AnalyticsComponent(
			amount = if (this.amount == null) "" else this.amount.toString(),
			buyerCountryCode = this.buyerCountry,
			type = ComponentType.MODAL.toString(),
			instanceId = this.instanceId.toString(),
			componentEvents = mutableListOf(event),
			offerType = this.offerType,
			__shared__ = dynamicKeys,
		)

		context?.let { AnalyticsLogger.getInstance(clientId = clientId).log(it, component) }
	}
}
