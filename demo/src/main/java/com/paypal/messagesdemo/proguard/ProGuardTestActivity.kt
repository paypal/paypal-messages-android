package com.paypal.messagesdemo.proguard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.paypal.messagesdemo.databinding.ActivityProguardTestBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * This activity serves as a test for ProGuard rules.
 * It exercises all the key libraries used in the project to ensure
 * the ProGuard rules are correctly keeping the necessary classes.
 */
class ProGuardTestActivity : AppCompatActivity() {
	private lateinit var binding: ActivityProguardTestBinding
	private val client = OkHttpClient()
	private val gson = GsonBuilder().create()
	private val testScope = CoroutineScope(Dispatchers.Main)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityProguardTestBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.testButton.setOnClickListener {
			runTests()
		}
	}

	private fun runTests() {
		binding.statusText.text = "Running tests..."

		testScope.launch {
			val results = mutableListOf<String>()

			try {
				// Test Gson
				results.add(testGson())

				// Test OkHttp
				results.add(testOkHttp())

				// Test Coroutines
				results.add(testCoroutines())

				// Update UI with all test results
				binding.statusText.text = results.joinToString("\n\n")
			} catch (e: Exception) {
				binding.statusText.text = "Test failed: ${e.message}"
				Log.e("ProGuardTest", "Test failed", e)
				// Don't show toast here - it might cause BadTokenException
			}
		}
	}

	private fun testGson(): String {
		try {
			val testData = TestData("test_value", 123)
			val json = gson.toJson(testData)
			val parsedData = gson.fromJson(json, TestData::class.java)

			return if (parsedData.stringValue == "test_value" && parsedData.intValue == 123) {
				"✅ Gson test passed"
			} else {
				"❌ Gson test failed: parsed data doesn't match original"
			}
		} catch (e: Exception) {
			Log.e("ProGuardTest", "Gson test failed", e)
			return "❌ Gson test failed: ${e.message}"
		}
	}

	private suspend fun testOkHttp(): String {
		return withContext(Dispatchers.IO) {
			try {
				val request = Request.Builder()
					.url("https://www.google.com")
					.build()

				client.newCall(request).execute().use { response ->
					if (response.isSuccessful) {
						"✅ OkHttp test passed"
					} else {
						"❌ OkHttp test failed: ${response.code}"
					}
				}
			} catch (e: IOException) {
				Log.e("ProGuardTest", "OkHttp test failed", e)
				"❌ OkHttp test failed: ${e.message}"
			}
		}
	}

	private suspend fun testCoroutines(): String {
		return withContext(Dispatchers.Default) {
			try {
				// Simulate some work
				val result = (1..1000).sum()
				if (result == 500500) {
					"✅ Coroutines test passed"
				} else {
					"❌ Coroutines test failed: wrong result"
				}
			} catch (e: Exception) {
				Log.e("ProGuardTest", "Coroutines test failed", e)
				"❌ Coroutines test failed: ${e.message}"
			}
		}
	}

	data class TestData(
		@SerializedName("string_value") val stringValue: String,
		@SerializedName("int_value") val intValue: Int,
	)
}
