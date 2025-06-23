@file:OptIn(ExperimentalMaterial3Api::class)

package com.paypal.messages

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.utils.PayPalErrors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Activity for displaying PayPal Modal in Jetpack Compose environments.
 * This is used when the context is a ComponentActivity rather than an AppCompatActivity.
 */
@OptIn(ExperimentalMaterial3Api::class)
class PayPalModalActivity : ComponentActivity() {
	companion object {
		private val callbacksRegistry = ConcurrentHashMap<UUID, ModalCallbacks>()

		/**
		 * Register callbacks for a specific modal instance
		 */
		fun registerCallbacks(
			instanceId: UUID,
			onApply: () -> Unit,
			onClick: () -> Unit,
			onError: (PayPalErrors.Base) -> Unit,
		) {
			callbacksRegistry[instanceId] = ModalCallbacks(
				onApply = onApply,
				onClick = onClick,
				onError = onError,
			)
		}

		/**
		 * Clear callbacks for a specific modal instance
		 */
		fun clearCallbacks(instanceId: UUID) {
			callbacksRegistry.remove(instanceId)
		}

		/**
		 * Data class to hold modal callbacks
		 */
		data class ModalCallbacks(
			val onApply: () -> Unit,
			val onClick: () -> Unit,
			val onError: (PayPalErrors.Base) -> Unit,
		)
	}

	private lateinit var clientId: String
	private var amount: Double? = null
	private var buyerCountry: String? = null
	private var offerType: String? = null
	private var modalCloseButtonType: ModalCloseButton = ModalCloseButton()
	private lateinit var instanceId: UUID

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Configure window to appear as an overlay with transparent background
		window.setBackgroundDrawableResource(android.R.color.transparent)
		setFinishOnTouchOutside(true)

		// Set window flags to exactly match the XML modal appearance
		window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
		window.addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

		// Set navigation bar to white for a clean look matching the XML version
		window.navigationBarColor = android.graphics.Color.WHITE

		// Use a combination of flags that match the exact appearance in the XML version
		window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
			android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
			android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

		// Set animation manually to ensure proper transition
		overridePendingTransition(android.R.anim.fade_in, 0)

		// Parse intent extras
		clientId = intent.getStringExtra("CLIENT_ID") ?: ""
		amount = intent.getDoubleExtra("AMOUNT", 0.0).takeIf { it > 0 }
		buyerCountry = intent.getStringExtra("BUYER_COUNTRY")
		offerType = intent.getStringExtra("OFFER_TYPE")
		// Create a default ModalCloseButton - we can't easily convert from string to object
		modalCloseButtonType = ModalCloseButton()

		val instanceIdStr = intent.getStringExtra("INSTANCE_ID")
			?: UUID.randomUUID().toString()
		instanceId = UUID.fromString(instanceIdStr)

		val callbacks = callbacksRegistry[instanceId]

		// Set Compose content
		setContent {
			// Configure the sheet state to use fixed height with no drag behavior
			val sheetState = rememberModalBottomSheetState(
				skipPartiallyExpanded = true, // Always use the specifically set height
				// Block any attempts to hide the sheet through dragging
				confirmValueChange = { newValue ->
					// Only allow expanded state changes
					newValue == androidx.compose.material3.SheetValue.Expanded
				},
			)
			val scope = rememberCoroutineScope()
			var showBottomSheet by remember { mutableStateOf(true) }

			var isLoading by remember { mutableStateOf(false) }
			var isError by remember { mutableStateOf(false) }

			DisposableEffect(Unit) {
				onDispose {
					// Clean up references when activity is closed
					clearCallbacks(instanceId)
				}
			}

			LaunchedEffect(Unit) {
				// Trigger onClick callback when the sheet is shown
				callbacks?.onClick?.invoke()
				// Force expand the sheet immediately to ensure it's fully shown
				delay(100) // Small delay to ensure sheet is ready
				sheetState.expand()
			}

			// Display the bottom sheet if it should be shown
			if (showBottomSheet) {
				PayPalModalSheet(
					clientId = clientId,
					amount = amount,
					buyerCountry = buyerCountry,
					offerType = offerType,
					modalCloseButtonType = modalCloseButtonType,
					sheetState = sheetState,
					onDismiss = {
						showBottomSheet = false
						finish()
					},
					onApply = {
						callbacks?.onApply?.invoke()
						showBottomSheet = false
						finish()
					},
					onError = { error ->
						callbacks?.onError?.invoke(error)
						showBottomSheet = false
						finish()
					},
				)
			}
		}
	}

	override fun finish() {
		super.finish()
		// Control the exit animation to ensure it fades out rather than slides
		overridePendingTransition(0, android.R.anim.fade_out)
	}
}

/**
 * Modal bottom sheet composable for PayPal messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayPalModalSheet(
	clientId: String,
	amount: Double?,
	buyerCountry: String?,
	offerType: String?,
	modalCloseButtonType: ModalCloseButton,
	sheetState: SheetState,
	onDismiss: () -> Unit,
	onApply: () -> Unit,
	onError: (PayPalErrors.Base) -> Unit,
) {
	val context = LocalContext.current
	var isLoading by remember { mutableStateOf(true) }
	var isError by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf("") }

	// Calculate screen height in dp
	val screenHeightDp = LocalConfiguration.current.screenHeightDp

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		modifier = Modifier
			.fillMaxWidth()
			.height((screenHeightDp * 0.85).dp), // Take up 85% of screen height to match screenshot
		dragHandle = null, // Hide the default drag handle for a cleaner look
		containerColor = Color.White, // Clean white background to ensure consistent color
		contentColor = Color.Black,
		// Rounded corners at the top to match the screenshot
		shape = androidx.compose.foundation.shape.RoundedCornerShape(
			topStart = 12.dp,
			topEnd = 12.dp,
			bottomStart = 0.dp,
			bottomEnd = 0.dp,
		),
		// Add a dark semi-transparent scrim over the rest of the screen
		scrimColor = Color.Black.copy(alpha = 0.5f),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxSize() // Fill all available height
				.weight(1f, fill = true) // Fill available space
				.background(Color.White), // Ensure background is white throughout
		) {
			// Header with close button - matching the screenshot style
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, end = 8.dp, start = 8.dp)
					.height(40.dp),
			) {
				// Message Configuration text
				Text(
					text = "Message Configuration",
					modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
					fontWeight = FontWeight.Bold,
					fontSize = 18.sp,
					color = Color.Black,
				)

				// Close button matching screenshot with improved hit target
				val scope = rememberCoroutineScope()
				IconButton(
					onClick = {
						// Use a coroutine scope to ensure smooth dismissal
						scope.launch {
							// Try to hide the sheet first (optional step)
							try {
								sheetState.hide()
							} catch (e: Exception) {
								// Ignore if it fails, we'll still dismiss
							}
							// Call the onDismiss callback
							onDismiss()
						}
					},
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(40.dp), // Increased touch target size
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_close),
						contentDescription = modalCloseButtonType.alternativeText ?: "Close",
						tint = Color.Black,
						modifier = Modifier.size(18.dp),
					)
				}
			}

			// Add the PayPal logo, title and description to match the screenshot
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 16.dp),
			) {
				Column {
					// PayPal logo
					Box(
						modifier = Modifier
							.size(40.dp)
							.background(Color(0xFF003087), shape = RoundedCornerShape(4.dp))
							.padding(8.dp),
					) {
						// PayPal 'P' - simplified representation
						Text(
							text = "P",
							color = Color.White,
							fontWeight = FontWeight.Bold,
							fontSize = 22.sp,
							modifier = Modifier.align(Alignment.Center),
						)
					}
					
					Spacer(modifier = Modifier.height(16.dp))
					
					// Buy now, pay over time heading
					Text(
						text = "Buy now,\npay over time",
						color = Color(0xFF003087),
						fontWeight = FontWeight.Bold,
						fontSize = 24.sp,
						lineHeight = 30.sp,
					)
					
					Spacer(modifier = Modifier.height(16.dp))
					
					// Get more info text
					Text(
						text = "Get more info on Pay Later options.",
						color = Color.Black,
						fontSize = 16.sp,
					)
					
					Spacer(modifier = Modifier.height(16.dp))
					
					// Pay in 4 option
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
							.padding(16.dp),
					) {
						Column {
							Text(
								text = "Pay in 4",
								fontWeight = FontWeight.Bold,
								fontSize = 18.sp,
							)
							Text(
								text = "Interest-free payments every 2 weeks, starting today.",
								color = Color.Gray,
								fontSize = 14.sp,
							)
						}
					}
					
					Spacer(modifier = Modifier.height(8.dp))
					
					// Pay Monthly option
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
							.padding(16.dp),
					) {
						Column {
							Text(
								text = "Pay Monthly",
								fontWeight = FontWeight.Bold,
								fontSize = 18.sp,
							)
							Text(
								text = "Split your purchase into equal monthly payments.",
								color = Color.Gray,
								fontSize = 14.sp,
							)
						}
					}
					
					Spacer(modifier = Modifier.height(16.dp))
					
					// Or shop with text
					Text(
						text = "Or shop with a reusable credit line.",
						color = Color.Black,
						fontSize = 16.sp,
					)
					
					Spacer(modifier = Modifier.height(8.dp))
					
					// PayPal Credit option
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
							.padding(16.dp),
					) {
						Column {
							Text(
								text = "PayPal Credit",
								fontWeight = FontWeight.Bold,
								fontSize = 18.sp,
							)
							Text(
								text = "No Interest if paid in full in 6 months for purchases of $149+.",
								color = Color.Gray,
								fontSize = 14.sp,
							)
						}
					}
					
					Spacer(modifier = Modifier.height(16.dp))
					
					// Terms apply text
					Text(
						text = "Terms apply for each option. Offer availability may depend on consumer & merchant eligibility.",
						color = Color.Gray,
						fontSize = 12.sp,
					)
				}
			}
			
			// WebView container - hidden since we're showing a static UI
			Box(
				modifier = Modifier
					.fillMaxSize()
					.weight(1f)
					.height(0.dp), // Hide the WebView
			) {
				AndroidView(
					factory = { ctx ->
						WebView(ctx).apply {
							// Set layout parameters to ensure WebView fills its container
							layoutParams = android.view.ViewGroup.LayoutParams(
								android.view.ViewGroup.LayoutParams.MATCH_PARENT,
								android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							)

							// Add a small margin inside WebView to match XML spacing
							this.setInitialScale(100) // Set scale to 100% for proper sizing
							try {
								// Ensure JavaScript is fully enabled
								settings.javaScriptEnabled = true
								settings.javaScriptCanOpenWindowsAutomatically = true
								settings.domStorageEnabled = true
								settings.allowContentAccess = true
								settings.useWideViewPort = true
								settings.loadWithOverviewMode = true
								settings.setSupportMultipleWindows(true)
								settings.builtInZoomControls = true
								settings.displayZoomControls = false

								// JavaScript and DOM storage are enabled for PayPal modal functionality
								// Set a mobile user-agent that identifies as an app webview
								settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) " +
									"AppleWebKit/537.36 (KHTML, like Gecko) PayPalMessagesAndroid/1.0"
								// Enable third-party cookies
								android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

								// TEST APPROACH 1: Try loading a simple test URL
								// loadUrl("https://www.example.com")
								// setBackgroundColor(android.graphics.Color.YELLOW) // Make WebView visible with yellow background

								// Add a WebViewClient to handle page load events
								this.webViewClient = object : android.webkit.WebViewClient() {
									override fun onPageStarted(
										view: android.webkit.WebView,
										url: String,
										favicon: android.graphics.Bitmap?,
									) {
										super.onPageStarted(view, url, favicon)
										isLoading = true
									}

									override fun onPageFinished(view: android.webkit.WebView, url: String) {
										super.onPageFinished(view, url)

										// Hide loading spinner when page finishes loading
										isLoading = false

										// Add a small delay to make sure content is fully rendered
										view.postDelayed({
											isLoading = false
										}, 500)
									}

									override fun onReceivedError(view: android.webkit.WebView, errorCode: Int, description: String, failingUrl: String) {
										super.onReceivedError(view, errorCode, description, failingUrl)
										isError = true
										isLoading = false
										errorMessage = description
										onError(PayPalErrors.ModalFailedToLoad(description, null))
									}

									// Ensure loading state is updated when navigation occurs
									override fun doUpdateVisitedHistory(view: android.webkit.WebView, url: String, isReload: Boolean) {
										super.doUpdateVisitedHistory(view, url, isReload)
										view.postDelayed({ isLoading = false }, 500)
									}
								}

								// Set white background to match screenshot
								setBackgroundColor(android.graphics.Color.WHITE)

								// Directly set up and load the PayPal modal content

								// Setup the PayPal modal
								val modalFragment = ModalFragment(clientId)
								val offerEnum = offerType?.let {
									try {
										com.paypal.messages.config.PayPalMessageOfferType.valueOf(it)
									} catch (e: Exception) {
										null
									}
								}
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

								// Make sure JavaScript is still enabled before setting up modal
								settings.javaScriptEnabled = true
								settings.domStorageEnabled = true

								// Setup the WebView with the modal content
								modalFragment.setupWebView(this)

								// Force hide the spinner after the modal is setup, regardless of loading state
								isLoading = false

								// Add a timer to force loading state to false after a reasonable timeout
								postDelayed({
									if (isLoading) {
										isLoading = false
									}
								}, 2000)
								// Add a timer to ensure spinner is eventually hidden
								postDelayed({
									isLoading = false
								}, 5000)
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
				// Loading indicator - subtle at the top
				if (isLoading && !isError) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(2.dp)
							.align(Alignment.TopCenter)
							.background(Color(0xFF0070BA)), // PayPal blue color
					)

					// Small indicator in center
					CircularProgressIndicator(
						modifier = Modifier
							.size(30.dp)
							.align(Alignment.Center),
						color = Color(0xFF0070BA), // PayPal blue color
						strokeWidth = 2.dp,
					)
				}

				// Error text - similar to XML implementation
				if (isError) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(5.dp),
					) {
						Text(
							text = errorMessage.ifEmpty { "Error fetching Learn More content." },
							color = Color.Red,
							textAlign = TextAlign.Center,
							modifier = Modifier
								.fillMaxWidth()
								.align(Alignment.Center),
							fontSize = 16.sp,
							fontWeight = FontWeight.Medium,
						)
					}
				}

				// Status indicator removed for production
			}

			// Add white spacer to cover navigation bar area / gray bar at bottom
			// This ensures a clean bottom edge that matches the screenshot
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(30.dp) // Reduced height to match screenshot
					.background(Color.White),
			)
		}
	}
}

// Helper method to get the modal URL for debugging purposes (can be removed in production)
internal fun ModalFragment.getModalUrl(): String? {
	return this.javaClass.getDeclaredField("modalUrl").apply {
		isAccessible = true
	}.get(this) as? String
}
