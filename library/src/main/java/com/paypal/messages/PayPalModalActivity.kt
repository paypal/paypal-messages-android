@file:OptIn(ExperimentalMaterial3Api::class)

package com.paypal.messages

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Activity for displaying PayPal Modal in Jetpack Compose environments.
 * This is used when the context is a ComponentActivity rather than an AppCompatActivity.
 */
class PayPalModalActivity : ComponentActivity() {
	companion object {
		private val callbacksRegistry = ConcurrentHashMap<UUID, ModalCallbacks>()

		// Track active modal instances for debugging purposes only
		private val activeModals = ConcurrentHashMap<UUID, Long>()

		// Track displayed modals to prevent duplicates
		private val displayedModals = java.util.Collections.synchronizedSet(HashSet<UUID>())

		// Cleanup method to remove stale entries
		private fun cleanupStaleEntries() {
			try {
				// Remove entries older than 5 minutes to prevent memory leaks
				val currentTime = System.currentTimeMillis()
				val iterator = activeModals.entries.iterator()
				while (iterator.hasNext()) {
					val entry = iterator.next()
					if (currentTime - entry.value > 5 * 60 * 1000) { // 5 minutes in milliseconds
						iterator.remove()
						displayedModals.remove(entry.key)
						android.util.Log.d("PayPalModalActivity", "Removed stale modal entry: ${entry.key}")
					}
				}
				android.util.Log.d(
					"PayPalModalActivity",
					"Active modals count: ${activeModals.size}, Displayed modals: ${displayedModals.size}, Callbacks count: ${callbacksRegistry.size}",
				)
			} catch (e: Exception) {
				android.util.Log.e("PayPalModalActivity", "Error cleaning up stale entries", e)
			}
		}

		/**
		 * Clear callbacks for a specific modal instance
		 */
		fun clearCallbacks(instanceId: UUID) {
			callbacksRegistry.remove(instanceId)
			activeModals.remove(instanceId)
			displayedModals.remove(instanceId)
			android.util.Log.d("PayPalModalActivity", "Cleared callbacks for instanceId $instanceId")
			cleanupStaleEntries()
		}

		/**
		 * Reset all active modals state - use this to force clean state
		 */
		fun resetAllModals() {
			android.util.Log.d(
				"PayPalModalActivity",
				"Resetting all modals state (before): " +
					"Active=${activeModals.size}, " +
					"Displayed=${displayedModals.size}, " +
					"Callbacks=${callbacksRegistry.size}",
			)
			activeModals.clear()
			displayedModals.clear()
			callbacksRegistry.clear()
			android.util.Log.d(
				"PayPalModalActivity",
				"All modals state reset (after): " +
					"Active=${activeModals.size}, " +
					"Displayed=${displayedModals.size}, " +
					"Callbacks=${callbacksRegistry.size}",
			)
		}

		/**
		 * Register callbacks for a specific modal instance
		 */
		fun registerCallbacks(
			instanceId: UUID,
			onApply: () -> Unit,
			onClick: () -> Unit,
			onError: (PayPalErrors.Base) -> Unit,
		) {
			val forceNew = callbacksRegistry[instanceId]?.forceNew ?: false

			// Check if this modal is already active - but always allow if FORCE_NEW is set
			if (activeModals.containsKey(instanceId) && !forceNew) {
				android.util.Log.d(
					"PayPalModalActivity",
					"Modal with instanceId $instanceId already has callbacks registered, updating them",
				)
			}

			// Track this modal with timestamp
			activeModals[instanceId] = System.currentTimeMillis()

			callbacksRegistry[instanceId] = ModalCallbacks(
				onApply = onApply,
				onClick = onClick,
				onError = onError,
			)
		}

		/**
		 * Data class to hold modal callbacks
		 */
		data class ModalCallbacks(
			val onApply: () -> Unit,
			val onClick: () -> Unit,
			val onError: (PayPalErrors.Base) -> Unit,
			val forceNew: Boolean = false,
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

		// Get instance ID first so we can check if this is a duplicate
		val instanceIdStr = intent.getStringExtra("INSTANCE_ID") ?: UUID.randomUUID().toString()
		instanceId = UUID.fromString(instanceIdStr)

		// Log that we're attempting to create a modal
		android.util.Log.d("PayPalModalActivity", "Creating PayPalModalActivity for instance: $instanceId")

		// Always clear any existing modal with the same ID to prevent conflicts
		cleanupStaleEntries()

		// Check if this modal is already displayed - if so, finish immediately
		if (displayedModals.contains(instanceId)) {
			android.util.Log.d("PayPalModalActivity", "Modal with instanceId $instanceId is already displayed, finishing")
			finish()
			return
		}

		// Check for FORCE_NEW flag
		val forceNew = intent.getBooleanExtra("FORCE_NEW", false)
		if (forceNew) {
			android.util.Log.d("PayPalModalActivity", "FORCE_NEW flag set, ensuring modal will be shown")
			// Remove any existing callbacks for this instance
			callbacksRegistry.remove(instanceId)
			// Remove from active modals tracking to prevent duplicates
			activeModals.remove(instanceId)
		}

		// Mark this instance as displayed to prevent duplicate modals
		displayedModals.add(instanceId)

		// Store this instance in activeModals with the current timestamp
		activeModals[instanceId] = System.currentTimeMillis()

		// Log how many modals are currently active for debugging
		android.util.Log.d("PayPalModalActivity", "Active modals: ${activeModals.size}, Displayed modals: ${displayedModals.size}")

		// Configure window to appear as an overlay with transparent background
		window.setBackgroundDrawableResource(android.R.color.transparent)
		setFinishOnTouchOutside(true)

		// Remove title at the top of the window
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

		// Set transparent navigation bar to avoid gray space at bottom
		window.navigationBarColor = android.graphics.Color.TRANSPARENT

		// Set animation manually to ensure proper transition
		overridePendingTransition(android.R.anim.fade_in, 0)

		// Parse intent extras - note that instanceId is already handled in the beginning of onCreate
		clientId = intent.getStringExtra("CLIENT_ID") ?: ""
		amount = intent.getDoubleExtra("AMOUNT", 0.0).takeIf { it > 0 }
		buyerCountry = intent.getStringExtra("BUYER_COUNTRY")
		offerType = intent.getStringExtra("OFFER_TYPE")
		// Create a default ModalCloseButton - we can't easily convert from string to object
		modalCloseButtonType = ModalCloseButton()

		// Get the registered callbacks (if any)
		val callbacks = callbacksRegistry[instanceId]

		// For logging only
		android.util.Log.d("PayPalModalActivity", "Created modal activity for instanceId: $instanceId, hasCallbacks: ${callbacks != null}")

		// Set Compose content
		setContent {
			var showDialog by remember { mutableStateOf(true) }
			var isLoading by remember { mutableStateOf(false) }
			var isError by remember { mutableStateOf(false) }
			var errorMessage by remember { mutableStateOf("") }

			// Clean up when the activity is destroyed
			DisposableEffect(Unit) {
				onDispose {
					// Clean up references when activity is closed
					PayPalModalActivity.clearCallbacks(instanceId)
				}
			}

			// Use a full screen Dialog that extends to the bottom of the screen
			if (showDialog) {
				// Use a Dialog implementation positioned at the bottom of the screen
				androidx.compose.ui.window.Dialog(
					onDismissRequest = {
						// Handle dismiss request (clicking outside)
						android.util.Log.d("PayPalModalActivity", "onDismiss called from Dialog, finishing activity")
						showDialog = false

						// Finish activity
						if (!isFinishing) {
							finish()
						}
					},
					properties = androidx.compose.ui.window.DialogProperties(
						// Allow dismissal when clicking outside
						dismissOnClickOutside = true,
						// Allow back button to dismiss
						dismissOnBackPress = true,
						// Use decorative window effects
						decorFitsSystemWindows = true,
						// Don't use secure flag for this dialog
						securePolicy = androidx.compose.ui.window.SecureFlagPolicy.Inherit,
						// Use default size behavior
						usePlatformDefaultWidth = false,
					),
				) {
					Column(
						modifier = Modifier.fillMaxSize(),
						verticalArrangement = Arrangement.Bottom,
					) {
						// Custom dialog content that lets the WebView handle all styling
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.fillMaxHeight(0.95f) // Takes 95% of available height
								.clip(
									RoundedCornerShape(
										topStart = 16.dp,
										topEnd = 16.dp,
									),
								), // Round the top corners
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

										// Do not set any background color - let the WebView display its own styling

										// Set initial scale for proper rendering
										setInitialScale(100)

										try {
											// Enable JavaScript and other web features
											settings.javaScriptEnabled = true
											settings.domStorageEnabled = true
											settings.javaScriptCanOpenWindowsAutomatically = true
											settings.loadsImagesAutomatically = true
											settings.useWideViewPort = true
											settings.loadWithOverviewMode = true
											settings.setSupportZoom(false)

											// Configure for better scrolling
											isVerticalScrollBarEnabled = true
											setVerticalScrollBarEnabled(true)
											setHorizontalScrollBarEnabled(false)
											setScrollBarStyle(android.view.View.SCROLLBARS_INSIDE_OVERLAY)

											// Hardware acceleration for performance
											setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

											// Standard mobile user agent
											settings.userAgentString =
												"Mozilla/5.0 (Linux; Android 11; Mobile) " +
												"AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.74 Mobile Safari/537.36"

											// Enable cookies
											android.webkit.CookieManager.getInstance()
												.setAcceptThirdPartyCookies(this, true)
											android.webkit.CookieManager.getInstance().setAcceptCookie(true)

											// Set WebViewClient to track loading state
											webViewClient = object : android.webkit.WebViewClient() {
												override fun onPageStarted(
													view: android.webkit.WebView,
													url: String,
													favicon: android.graphics.Bitmap?,
												) {
													super.onPageStarted(view, url, favicon)
													isLoading = true
												}

												override fun onPageFinished(
													view: android.webkit.WebView,
													url: String,
												) {
													super.onPageFinished(view, url)
													isLoading = false
												}

												override fun onReceivedError(
													view: android.webkit.WebView,
													errorCode: Int,
													description: String,
													failingUrl: String,
												) {
													super.onReceivedError(view, errorCode, description, failingUrl)
													isError = true
													isLoading = false
													errorMessage = description
													callbacks?.onError?.invoke(
														PayPalErrors.ModalFailedToLoad(
															description,
															null,
														),
													)
												}
											}

											// Create and setup the modal fragment
											val modalFragment = ModalFragment.newInstance(clientId)
											val offerEnum = offerType?.let {
												try {
													com.paypal.messages.config.PayPalMessageOfferType.valueOf(it)
												} catch (e: Exception) {
													null
												}
											}

											// Initialize the modal configuration
											val modalConfig = ModalConfig(
												amount = amount,
												buyerCountry = buyerCountry,
												offer = offerEnum,
												ignoreCache = false,
												devTouchpoint = false,
												stageTag = null,
												events = ModalEvents(
													onApply = {
														android.util.Log.d("PayPalModalActivity", "onApply called")
														// Invoke callback first
														callbacks?.onApply?.invoke()
														showDialog = false
														// Finish immediately
														if (!isFinishing) {
															finish()
														}
													},
													onClick = {},
													onError = { error ->
														isError = true
														isLoading = false
														errorMessage = error.message ?: "Unknown error"
														// Invoke error callback
														callbacks?.onError?.invoke(error)
														showDialog = false
														// Finish immediately
														if (!isFinishing) {
															finish()
														}
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

											// Setup the WebView with the PayPal modal content
											modalFragment.setupWebView(this)

											// Ensure clickability
											isClickable = true
											isFocusable = true
											isFocusableInTouchMode = true

											// Force hide spinner after a timeout
											postDelayed({ isLoading = false }, 5000)
										} catch (e: Exception) {
											android.util.Log.e(
												"PayPalModalActivity",
												"Error setting up WebView",
												e,
											)
											isError = true
											isLoading = false
											errorMessage = e.message ?: "Unknown error"
											callbacks?.onError?.invoke(
												PayPalErrors.ModalFailedToLoad(
													e.message ?: "Unknown error",
													null,
												),
											)
										}
									}
								},
								modifier = Modifier.fillMaxSize(),
								update = { /* No-op */ },
							)

							// Close button positioned in bottom corner directly in content box
							// We're positioning the close button directly in the main content box at bottom right
							IconButton(
								onClick = {
									android.util.Log.d("PayPalModalActivity", "Close button clicked")
									showDialog = false
									// Finish immediately without delay
									if (!isFinishing) {
										finish()
									}
								},
								modifier = Modifier
									.align(Alignment.TopEnd)
									.size(40.dp),
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_close),
									contentDescription = modalCloseButtonType.alternativeText ?: "Close",
									tint = Color.Black,
									modifier = Modifier.size(18.dp),
								)
							}

							// Loading indicator
							if (isLoading && !isError) {
								Box(
									contentAlignment = Alignment.Center,
									modifier = Modifier.fillMaxSize(),
								) {
									CircularProgressIndicator(
										modifier = Modifier.size(30.dp),
										color = Color(0xFF0070BA), // PayPal blue color
										strokeWidth = 2.dp,
									)
								}
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
										textAlign = TextAlign.Center,
										fontSize = 16.sp,
										fontWeight = FontWeight.Medium,
									)
								}
							}
						}
					}
				}
			}
		}
	}

	@Deprecated("Deprecated in Java")
	override fun onBackPressed() {
		android.util.Log.d("PayPalModalActivity", "Back button pressed")
		finish()
	}

	override fun finish() {
		// Clear callbacks for this instance before finishing to prevent memory leaks
		try {
			android.util.Log.d("PayPalModalActivity", "Finishing activity, clearing callbacks for instance: $instanceId")
			PayPalModalActivity.clearCallbacks(instanceId)
		} catch (e: Exception) {
			android.util.Log.e("PayPalModalActivity", "Error clearing callbacks", e)
		}

		super.finish()

		// Control the exit animation to ensure it fades out rather than slides
		overridePendingTransition(0, android.R.anim.fade_out)
	}

	override fun onDestroy() {
		// Additional safety check to clear callbacks and active modal tracking
		try {
			PayPalModalActivity.clearCallbacks(instanceId)
		} catch (e: Exception) {
			android.util.Log.e("PayPalModalActivity", "Error clearing callbacks on destroy", e)
		}
		super.onDestroy()
	}
}

/**
 * Helper method to get the modal URL for debugging purposes (can be removed in production)
 */
internal fun ModalFragment.getModalUrl(): String? {
	return this.javaClass.getDeclaredField("modalUrl").apply {
		isAccessible = true
	}.get(this) as? String
}
