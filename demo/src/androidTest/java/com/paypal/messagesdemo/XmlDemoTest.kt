package com.paypal.messagesdemo

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messagesdemo.R as Demo

// Check Text Color
object ColorMatcher {
	fun withTextColor(
		@ColorInt expectedColor: Int,
	): TypeSafeMatcher<View> {
		return object : TypeSafeMatcher<View>() {
			override fun describeTo(description: Description) {
				description.appendText("with text color: ")
				description.appendValue(expectedColor)
			}

			override fun matchesSafely(view: View): Boolean {
				if (view is TextView) {
					return view.currentTextColor == expectedColor
				}
				return false
			}
		}
	}
}

// Check Positioning
object GravityMatcher {
	fun withGravity(expectedGravity: Int): TypeSafeMatcher<View> {
		return object : TypeSafeMatcher<View>() {
			override fun describeTo(description: Description) {
				description.appendText("with gravity: ")
				description.appendValue(expectedGravity)
			}

			override fun matchesSafely(view: View): Boolean {
				if (view is TextView) {
					return (view.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) == expectedGravity
				}
				return false
			}
		}
	}
}

// Custom ViewAction to wait
fun waitFor(millis: Long): ViewAction {
	return object : ViewAction {
		override fun getConstraints(): Matcher<View> {
			return isRoot()
		}

		override fun getDescription(): String {
			return "wait for $millis milliseconds"
		}

		override fun perform(uiController: UiController, view: View) {
			uiController.loopMainThreadForAtLeast(millis)
		}
	}
}

fun clickMessage() {
	onView(withId(R.id.content)).perform(click())
	waitFor(500)
}

fun submit() {
	onView(withId(Demo.id.submit)).perform(scrollTo())
	onView(withId(Demo.id.submit)).perform(click())
	waitFor(500)
}

fun checkPi4TilePresent() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Interest-free payments every 2 weeks, starting today.")),
	)
}

fun checkPayMonthlyTilePresent() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Split your purchase into equal monthly payments.")),
	)
}

fun checkNiTilePresent() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("No Interest if paid in full in 6 months for purchases of \$99+.")),
	)
}

fun closeButtonPresent() {
	onView(withId(R.id.ModalCloseButton)).check(
		matches(ViewMatchers.isDisplayed()),
	)
}

fun clickTileByIndex(index: Int) {
	onWebView(ViewMatchers.withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.CSS_SELECTOR, ".tile:nth-of-type($index)"))
		.perform(DriverAtoms.webClick())
}

fun clickPi4Tile() {
	clickTileByIndex(1)
}

fun clickPayMonthlyTile() {
	clickTileByIndex(2)
}

fun clickNiTile() {
	clickTileByIndex(3)
}

fun testDisclosure() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Find more disclosures")),
	)
}

fun clickSeeOtherModalOptions() {
	onWebView(
		ViewMatchers.withId(R.id.ModalWebView),
	).withElement(DriverAtoms.findElement(Locator.ID, "productListLink")).perform(DriverAtoms.webClick())
}

fun clickDisclosure() {
	onWebView(
		ViewMatchers.withId(R.id.ModalWebView),
	).forceJavascriptEnabled().withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "a")).perform(DriverAtoms.webClick())
}

fun modalContent(expectedText: String) {
	onWebView(ViewMatchers.withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body"))
		.check(WebViewAssertions.webMatches(DriverAtoms.getText(), containsString(expectedText)))
}

fun typeCalculatorAmount(amount: String) {
	onWebView(withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.CSS_SELECTOR, ".input ")) // Change to your input box selector
		.perform(DriverAtoms.webKeys(amount))
}

fun clearCalculatorAmount() {
	onWebView(withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.CSS_SELECTOR, ".input ")) // Change to your input box selector
		.perform(DriverAtoms.clearElement())
}

fun checkPi4ModalContent() {
	modalContent("Pay in 4")
}

fun checkPayMonthlyContent() {
	modalContent("Pay Monthly")
}

fun checkNIContent() {
	modalContent("Credit")
}

fun closeModal() {
	onView(withId(R.id.ModalCloseButton)).perform(click())
}

fun clickOffer(offerId: Int) {
	onView(withId(offerId)).perform(click())
}

fun clickShortTermOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerShortTerm)
}

fun clickLongTermOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerLongTerm)
}

fun clickPayIn1() {
	clickOffer(com.paypal.messagesdemo.R.id.offerPayIn1)
}

fun clickNIOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerCredit)
}

fun typeAmount(text: String) {
	onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(typeText(text))
}

fun clearAmount() {
	onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(clearText())
}

fun checkMessage(text: String) {
	onView(withId(R.id.content)).check(matches(withText(containsString(text))))
}

fun checkMessageColor(activityScenarioRule: ActivityScenarioRule<*>, color: PayPalMessageColor) {
	var expectedColor: Int? = null

	// Get the actual color value from the resource ID
	activityScenarioRule.scenario.onActivity { activity ->
		expectedColor = ContextCompat.getColor(activity, color.colorResId)
	}

	// Use the custom matcher to check the text color of the TextView
	onView(withId(R.id.content))
		.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

	@Test
	fun testGenericMessage() {
		// Perform a delay
		waitFor(500)

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.BLACK.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}

	@Test
	fun testGenericMessageAndModal() {
		waitFor(500)

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		modalContent("Pay Later options")
	}

	@Test
	fun testGenericModalNavigatingTiles() {
		waitFor(500)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeButtonPresent()
		checkPi4TilePresent()
		checkPayMonthlyTilePresent()
		checkNiTilePresent()

		clickNiTile()
		checkNIContent()
		clickSeeOtherModalOptions()

		clickPi4Tile()
		checkPi4ModalContent()
		clickSeeOtherModalOptions()

		clickPayMonthlyTile()
		checkPayMonthlyContent()
		clickSeeOtherModalOptions()

		closeModal()
	}

	@Test
	fun testGenericModalClose() {
		waitFor(500)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeModal()
	}

	@Test
	fun testGenericModalCloseAndOpenSameMessage() {
		waitFor(500)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		clickPi4Tile()
		checkPi4ModalContent()
		closeModal()

		clickMessage()
		checkPi4ModalContent()
	}

	@Test
	fun testShorTermMessage() {
		waitFor(500)
		clickShortTermOffer()
		submit()
		waitFor(500)
	}

	@Test
	fun testShortTermNonQualifyingMessage() {
		waitFor(500)
		clickShortTermOffer()

		typeAmount("15")
		submit()
		waitFor(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))
		waitFor(500)
		clearAmount()
		typeAmount("2000")
		submit()
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))

		waitFor(500)
	}

	@Test
	fun testShortTermQualifyingMessage() {
		waitFor(500)
		clickShortTermOffer()

		typeAmount("1000")
		submit()
		waitFor(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("250"))))
// 		checkMessage("250")
// 		waitFor(5000)
	}

	@Test
	fun testShortTermMessageAndModal() {
		waitFor(500)
		clickShortTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPi4ModalContent()
		closeModal()
	}

	@Test
	fun testShortTermOpenAndSwitchModal() {
		waitFor(500)
		clickShortTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPi4ModalContent()
		clickSeeOtherModalOptions()
		waitFor(200)
		clickPi4Tile()
		checkPi4ModalContent()
	}

	@Test
	fun testShortTermQualifyingModal() {
		waitFor(500)
		clickShortTermOffer()
		typeAmount("1000")
		submit()
		waitFor(500)
		clickMessage()
		checkPi4ModalContent()
	}

	@Test
	fun testLongTermNonQualifyingMessage() {
		waitFor(500)
		clickLongTermOffer()
		typeAmount("15")
		submit()
		waitFor(500)
		checkMessage("%paypal_logo% Pay monthly")
		clearAmount()
		typeAmount("20000")
		submit()
		checkMessage("%paypal_logo% Pay monthly")
	}

	@Test
	fun testLongTermQualifyingMessage() {
		waitFor(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitFor(500)
		checkMessage("$95.55")
	}

	@Test
	fun testLongTermNoAmountMessage() {
		waitFor(500)
		clickLongTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("Enter an amount of")
	}

	@Test
	fun testLongTermQualifyingAmountMessage() {
		waitFor(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitFor(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("12 months")
	}

	@Test
	fun testLongTermRangeMessage() {
		waitFor(500)
		clickLongTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPayMonthlyContent()

		typeCalculatorAmount("1000")
		waitFor(5000)
		modalContent("for 12")

		clearCalculatorAmount()
		waitFor(500)
		typeCalculatorAmount("100")
		waitFor(5000)
		modalContent("Enter an amount")

		clearCalculatorAmount()
		typeCalculatorAmount("20000")
		waitFor(5000)
		modalContent("Enter an amount no larger")
		waitFor(500)
	}

	@Test
	fun testLongTermModalNavigation() {
		waitFor(500)
		clickLongTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPayMonthlyContent()

		clickSeeOtherModalOptions()
		clickPayMonthlyTile()
		checkPayMonthlyContent()
	}

	@Test
	fun testLongTermModalDisclosure() {
		waitFor(500)
		clickLongTermOffer()
		submit()
		waitFor(500)
		clickMessage()
		checkPayMonthlyContent()
		waitFor(5000)

		onWebView().forceJavascriptEnabled()

		modalContent("Find more disclosures")

// 		clickDisclosure()
// 		onView(isRoot()).perform(waitFor(50000))

// 		pressBack()
// 		waitFor(5000)

// 		checkPayMonthlyContent()
	}

	@Test
	fun testNiMessage() {
		waitFor(500)
		waitFor(500)
		clickNIOffer()
		submit()
		waitFor(1000)

		checkMessage("No Interest")
	}
}

// @RunWith(AndroidJUnit4ClassRunner::class)
// public class XmlDemoNiTest {
// 	var expectedColor: Int? = null
//
// 	@get:Rule
// 	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)
//
// 	@Before
// 	fun setup() {
// 		activityScenarioRule.scenario.onActivity { activity ->
// 			activity.environment =  PayPalEnvironment.LIVE
// 		}
// 	}
//
// 	@Test
// 	fun testNiMessage(){
// 		waitFor(500)
// 		waitFor(500)
// 		clickNIOffer()
// 		submit()
// 		waitFor(1000)
//
// 		checkMessage("No Interest")
// 	}
// }

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoStyleOptionsTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

	@Test
	fun testGenericMessage() {
		// Perform a delay
		waitFor(500)

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.BLACK.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}

	@Test
	fun testAlignment() {
		waitFor(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		onView(withId(Demo.id.styleCenter)).perform(click())
		submit()
		waitFor(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.CENTER.value)))

		onView(withId(Demo.id.styleRight)).perform(click())
		submit()
		waitFor(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.RIGHT)))
	}

	@Test
	fun testMessageColors() {
		waitFor(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)

		onView(withId(Demo.id.styleWhite)).perform(click())
		submit()
		waitFor(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.WHITE)

		onView(withId(Demo.id.styleMonochrome)).perform(click())
		submit()
		waitFor(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.MONOCHROME)

		onView(withId(Demo.id.styleGrayscale)).perform(click())
		submit()
		waitFor(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.GRAYSCALE)

		onView(withId(Demo.id.styleBlack)).perform(click())
		submit()
		waitFor(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)
	}

	@Test
	fun testLogoAlignment() {
		waitFor(500)
		onView(withId(Demo.id.styleAlternative)).perform(click())
		submit()
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")

		onView(withId(Demo.id.styleInline)).perform(click())
		submit()
		checkMessage("Buy now, pay later with %paypal_logo%. Learn more")

		onView(withId(Demo.id.styleNone)).perform(click())
		submit()
		checkMessage("Buy now, pay later with PayPal. Learn more")

		onView(withId(Demo.id.stylePrimary)).perform(click())
		submit()
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
	}

	// @Test
	// fun testCrossBorder(){
	// 	activityScenarioRule.scenario.onActivity { activity ->
	// 		activity.environment = PayPalEnvironment.stage(("msmaster.qa.paypal.com"))
	// 	}

	// 	waitFor(50000)
	// 	onView(withId(Demo.id.styleAlternative)).perform(click())
	// 	submit()
	// 	checkMessage("%paypal_logo% Buy now, pay later. Learn more")
	// 	waitFor(5000)

	// }

// 	@Test
// 	fun testGenericInlineLogoBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleInline)).perform(click())
// 		submit()
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("Buy now, pay later with %paypal_logo%. Learn more")
// 	}
//
// 	@Test
// 	fun testGenericAlternativeLogoBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleAlternative)).perform(click())
// 		submit()
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
// 	}
//
// 	@Test
// 	fun testGenericNoneLogoBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleNone)).perform(click())
// 		submit()
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		onView(withId(R.id.content)).check(matches(withText("Buy now, pay later with PayPal. Learn more")))
// 	}
//
// 	@Test
// 	fun testGenericRightAlignmentBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleRight)).perform(click())
// 		submit()
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
// 		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.RIGHT)))
// 	}
//
// 	@Test
// 	fun testGenericCenterAlignmentBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleCenter)).perform(click())
// 		submit()
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
// 		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.CENTER.value)))
// 	}
//
// 	@Test
// 	fun testGenericWhiteBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleWhite)).perform(click())
// 		submit()
//
// 		// Get the actual color value from the resource ID
// 		activityScenarioRule.scenario.onActivity { activity ->
// 			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.WHITE.colorResId)
// 		}
//
// 		// Use the custom matcher to check the text color of the TextView
// 		onView(withId(R.id.content))
// 			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
// 	}
//
// 	@Test
// 	fun testGenericMonochromeBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleMonochrome)).perform(click())
// 		submit()
//
// 		// Get the actual color value from the resource ID
// 		activityScenarioRule.scenario.onActivity { activity ->
// 			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.MONOCHROME.colorResId)
// 		}
//
// 		// Use the custom matcher to check the text color of the TextView
// 		onView(withId(R.id.content))
// 			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
// 	}
//
// 	@Test
// 	fun testGenericGrayscaleBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitFor(500)
// 		onView(withId(Demo.id.styleGrayscale)).perform(click())
// 		submit()
//
// 		// Get the actual color value from the resource ID
// 		activityScenarioRule.scenario.onActivity { activity ->
// 			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.GRAYSCALE.colorResId)
// 		}
//
// 		// Use the custom matcher to check the text color of the TextView
// 		onView(withId(R.id.content))
// 			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
// 	}
}
