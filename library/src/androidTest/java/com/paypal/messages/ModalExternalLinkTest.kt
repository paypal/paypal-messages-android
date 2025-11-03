package com.paypal.messages

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class ModalExternalLinkTest {
	@Test
	fun targetBlankLink_opensExternalBrowserIntent() {
		// Use a simple activity to provide a valid UI thread/context
		TestActivity.layout = android.R.layout.simple_list_item_1
		val latch = CountDownLatch(1)
		val capturedIntent = AtomicReference<Intent?>()
		ActivityScenario.launch(TestActivity::class.java).use { scenario ->
			scenario.onActivity { activity ->

				// Wrap the Activity context so we can intercept startActivity calls
				class RecordingContext(base: Context) : ContextWrapper(base) {
					override fun startActivity(intent: Intent?) {
						capturedIntent.set(intent)
						latch.countDown()
					}
				}

				val recordingContext = RecordingContext(activity)
				val webView = WebView(recordingContext)

				// Initialize the modal WebView configuration
				val fragment = ModalFragment.newInstance(clientId = "test-client-id")
				fragment.setupWebView(webView)

				// Load a minimal page that triggers a target=_blank navigation
				val html = """
					<html>
					  <body>
					    <a id=\"lnk\" href=\"https://example.com/disclosures\" target=\"_blank\">open</a>
					    <script>
					      setTimeout(function(){ document.getElementById('lnk').click(); }, 250);
					    </script>
					  </body>
					</html>
				""".trimIndent()

				webView.loadDataWithBaseURL(
					"https://www.paypal.com/credit-presentment/lander/modal",
					html,
					"text/html",
					"UTF-8",
					null,
				)
			}

			// Wait for the external intent to be captured
			assertTrue("Expected an ACTION_VIEW intent to be fired", latch.await(5, TimeUnit.SECONDS))
			val intent = capturedIntent.get()
			assertEquals(Intent.ACTION_VIEW, intent?.action)
			assertEquals("https://example.com/disclosures", intent?.data.toString())
		}
	}
}
