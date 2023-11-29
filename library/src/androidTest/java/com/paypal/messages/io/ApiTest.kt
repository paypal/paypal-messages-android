import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.LocalStorage
import com.paypal.messages.logger.TrackingPayload
import com.paypal.messages.model.ApiHashData
import com.paypal.messages.model.ApiMessageData
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import com.paypal.messages.utils.generateUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ApiTest {

	private val context = InstrumentationRegistry.getTargetContext()
	private val localStorage = LocalStorage(context)
	private val gson = Gson()
	private val api = Api()

	@Before
	fun setUp() {
		localStorage.merchantHash = null
		localStorage.ageOfMerchantHash = 0
	}

	@Test
	fun getMessageWithHash_noMerchantHash_fetchesNewHashAndReturnsData() {
		runBlocking {
			val messageConfig = MessageConfig.Builder()
				.setClientId("CLIENT_ID")
				.build()

			val onActionCompleted = object : Api.OnActionCompleted {
				override fun onActionCompleted(result: ApiResult) {
					assertNotNull(result)
					assertTrue(result is ApiResult.Success<*>)
					val data = result.response as ApiMessageData.Response
					assertNotNull(data.content)
					assertNotNull(data.meta)
				}
			}

			api.getMessageWithHash(context, messageConfig, onActionCompleted)
		}
	}

	@Test
	fun getMessageWithHash_merchantHashNotExpired_usesLocalHashAndReturnsData() {
		runBlocking {
			val messageConfig = MessageConfig.Builder()
				.setClientId("CLIENT_ID")
				.build()

			val merchantHash = generateUUID().toString()
			localStorage.merchantHash = merchantHash
			localStorage.ageOfMerchantHash = 1000L

			val onActionCompleted = object : Api.OnActionCompleted {
				override fun onActionCompleted(result: ApiResult) {
					assertNotNull(result)
					assertTrue(result is ApiResult.Success<*>)
					val data = result.response as ApiMessageData.Response
					assertNotNull(data.content)
					assertNotNull(data.meta)
				}
			}

			api.getMessageWithHash(context, messageConfig, onActionCompleted)
		}
	}

	@Test
	fun getMessageWithHash_merchantHashExpired_fetchesNewHashAndReturnsData() {
		runBlocking {
			val messageConfig = MessageConfig.Builder()
				.setClientId("CLIENT_ID")
				.build()

			val merchantHash = generateUUID().toString()
			localStorage.merchantHash = merchantHash
			localStorage.ageOfMerchantHash = 1000000L

			val onActionCompleted = object : Api.OnActionCompleted {
				override fun onActionCompleted(result: ApiResult) {
					assertNotNull(result)
					assertTrue(result is ApiResult.Success<*>)
					val data = result.response as ApiMessageData.Response
					assertNotNull(data.content)
					assertNotNull(data.meta)
				}
			}

			api.getMessageWithHash(context, messageConfig, onActionCompleted)
		}
	}

	@Test
	fun getMessageWithHash_failedToFetchData_returnsFailure() {
		runBlocking {
			val messageConfig = MessageConfig.Builder()
				.setClientId("CLIENT_ID")
				.build()

			val onActionCompleted = object : Api.OnActionCompleted {
				override fun onActionCompleted(result: ApiResult) {
					assertNotNull(result)
					assertTrue(result is ApiResult.Failure<*>)
