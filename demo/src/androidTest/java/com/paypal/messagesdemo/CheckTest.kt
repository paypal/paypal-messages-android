package com.paypal.messagesdemo

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messagesdemo.R as Demo

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

@RunWith(AndroidJUnit4ClassRunner::class)
public class CheckTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

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

	fun submit() {
		onView(withId(Demo.id.submit)).perform(scrollTo())
		onView(withId(Demo.id.submit)).perform(click())
		onView(isRoot()).perform(waitFor(500))
	}

	@Test
	fun testGenericBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
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
	fun testGenericInlineLogoBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleInline)).perform(click())
		submit()

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("Buy now, pay later with %paypal_logo%. Learn more")))
	}

	@Test
	fun testGenericAlternativeLogoBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleAlternative)).perform(click())
		submit()

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
	}

	@Test
	fun testGenericNoneLogoBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleNone)).perform(click())
		submit()

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("Buy now, pay later with PayPal. Learn more")))
	}

	@Test
	fun testGenericRightAlignmentBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleRight)).perform(click())
		submit()

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.RIGHT.value)))
	}

	@Test
	fun testGenericCenterAlignmentBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleCenter)).perform(click())
		submit()

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.CENTER.value)))
	}

	@Test
	fun testGenericWhiteBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleWhite)).perform(click())
		submit()

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.WHITE.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}
	
	// MONOCHROME
	// GRAYSCALE
}
