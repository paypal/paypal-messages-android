package com.paypal.messages

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.utils.PayPalErrors

/**
 * Custom modal content composable for PayPal messages
 */
@Composable
fun PayPalComposableModal(
	clientId: String,
	amount: Double?,
	buyerCountry: String?,
	offerType: String?,
	modalCloseButtonType: ModalCloseButton,
	onDismiss: () -> Unit,
	onApply: () -> Unit,
	onError: (PayPalErrors.Base) -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	var isLoading by remember { mutableStateOf(true) }
	var isError by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf("") }

	// Create the ModalFragment instance for WebView setup
	// Use clientId as key to ensure fragment is recreated if clientId changes
	// This also ensures proper recreation when "Don't keep activities" is enabled
	val modalFragment = remember(clientId) { ModalFragment.newInstance(clientId) }
	val offerEnum = offerType?.let {
		try {
			PayPalMessageOfferType.valueOf(it)
		} catch (e: Exception) {
			null
		}
	}

	// Initialize the modal
	LaunchedEffect(Unit) {
		val modalConfig = ModalConfig(
			amount = amount,
			buyerCountry = buyerCountry,
			offer = offerEnum,
			ignoreCache = false,
			devTouchpoint = false,
			stageTag = null,
			events = ModalEvents(
				onApply = onApply,
				onClick = {},
				onError = {
					isError = true
					isLoading = false
					errorMessage = it.message ?: "Unknown error"
					onError(it)
				},
				onLoading = {
					isLoading = true
					isError = false
				},
				onSuccess = {
					isLoading = false
					isError = false
				},
			),
			modalCloseButton = modalCloseButtonType,
		)
		modalFragment.init(modalConfig)
	}

	// Full screen container with rounded corners
	Box(
		modifier = modifier
			.fillMaxWidth()
			.fillMaxHeight() // Fill the entire height
			.background(
				color = Color(0xFFF0F4F9), // Light blue background matching screenshot
				shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
			)
			.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
	) {
		// WebView container - takes up entire space
		AndroidView(
			factory = { ctx ->
				WebView(ctx).apply {
					// Set layout parameters for full screen
					layoutParams = android.view.ViewGroup.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					)

					// Set WebView background to match container
					setBackgroundColor(android.graphics.Color.parseColor("#F0F4F9"))

					// Set initial scale for proper rendering
					setInitialScale(100)

					try {
						// Enable JavaScript and other web features
						settings.javaScriptEnabled = true
						settings.domStorageEnabled = true
						settings.javaScriptCanOpenWindowsAutomatically = true
						settings.loadsImagesAutomatically = true

						// Configure for better scrolling
						isVerticalScrollBarEnabled = true
						setVerticalScrollBarEnabled(true)
						setHorizontalScrollBarEnabled(false)
						setScrollBarStyle(android.view.View.SCROLLBARS_INSIDE_OVERLAY)

						// Hardware acceleration for performance
						setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

						// Standard mobile user agent
						settings.userAgentString = "Mozilla/5.0 (Linux; Android 11; Mobile) " +
							"AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.74 Mobile Safari/537.36"

						// Enable cookies
						android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
						android.webkit.CookieManager.getInstance().setAcceptCookie(true)

						// Set WebViewClient to track loading state
						webViewClient = object : android.webkit.WebViewClient() {
							override fun onPageStarted(view: android.webkit.WebView, url: String, favicon: android.graphics.Bitmap?) {
								super.onPageStarted(view, url, favicon)
								isLoading = true
							}

							override fun onPageFinished(view: android.webkit.WebView, url: String) {
								super.onPageFinished(view, url)
								isLoading = false
							}

							override fun onReceivedError(view: android.webkit.WebView, errorCode: Int, description: String, failingUrl: String) {
								super.onReceivedError(view, errorCode, description, failingUrl)
								isError = true
								isLoading = false
								errorMessage = description
								onError(PayPalErrors.ModalFailedToLoad(description, null))
							}
						}

						// Setup the WebView with the PayPal modal content
						modalFragment.setupWebView(this)

						// Ensure clickability
						isClickable = true
						isFocusable = true
						isFocusableInTouchMode = true

						// Force hide spinner after a timeout
						postDelayed({ isLoading = false }, 5000)
					} catch (e: Exception) {
						android.util.Log.e("PayPalModalActivity", "Error setting up WebView", e)
						isError = true
						isLoading = false
						errorMessage = e.message ?: "Unknown error"
						onError(PayPalErrors.ModalFailedToLoad(e.message ?: "Unknown error", null))
					}
				}
			},
			modifier = Modifier.fillMaxSize(),
			update = { /* No-op */ },
		)

		// Close button overlay at the top
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, end = 8.dp)
				.height(40.dp),
		) {
			// Close button
			IconButton(
				onClick = {
					android.util.Log.d("PayPalModalActivity", "Close button clicked")
					onDismiss()
				},
				modifier = Modifier
					.align(Alignment.TopEnd)
					.size(40.dp)
					.testTag("closeButton"),
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_close),
					contentDescription = modalCloseButtonType.alternativeText ?: "Close",
					tint = Color.Black,
					modifier = Modifier.size(18.dp),
				)
			}
		}

		// Loading indicator
		if (isLoading && !isError) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(2.dp)
					.align(Alignment.TopCenter)
					.background(Color(0xFF0070BA)), // PayPal blue color
			)

			CircularProgressIndicator(
				modifier = Modifier
					.size(30.dp)
					.align(Alignment.Center)
					.testTag("progressIndicator"),
				color = Color(0xFF0070BA), // PayPal blue color
				strokeWidth = 2.dp,
			)
		}

		// Error display
		if (isError) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = errorMessage.ifEmpty { "Error fetching PayPal content." },
					color = Color.Red,
					modifier = Modifier.testTag("errorText"),
					textAlign = TextAlign.Center,
					fontSize = 16.sp,
					fontWeight = FontWeight.Medium,
				)
			}
		}
	}
}
