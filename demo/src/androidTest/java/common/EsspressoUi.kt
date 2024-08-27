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
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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

fun waitForApp(millis: Long) {
	onView(isRoot()).perform(waitFor(millis))
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
	).withElement(DriverAtoms.findElement(Locator.XPATH, "/html/body/div/div[3]/div/div[5]/main/div/div[2]/a")).perform(DriverAtoms.webClick())
// 	onWebView(
// 		ViewMatchers.withId(R.id.ModalWebView),
// 	).forceJavascriptEnabled().withElement(DriverAtoms.findElement(Locator.XPATH, "/html/body/div/div[3]/div/div[5]/main/div/div[2]/a")).perform(DriverAtoms.webClick())
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

fun clickApplyNow() {
	onWebView(withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.CSS_SELECTOR, "button.button")) // Change to your input box selector
		.perform(DriverAtoms.webClick())
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
