package com.paypal.messagesdemo

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso
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

private fun clickMessage() {
	onView(withId(R.id.content)).perform(click())
	onView(isRoot()).perform(waitFor(500))
}

private fun submit() {
	onView(withId(Demo.id.submit)).perform(scrollTo())
	onView(withId(Demo.id.submit)).perform(click())
	onView(isRoot()).perform(waitFor(500))
}

private fun testPi4Present() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Interest-free payments every 2 weeks, starting today.")),
	)
}

private fun testPayMonthlyPresent() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Split your purchase into equal monthly payments.")),
	)
}

private fun testNIPresent() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("No Interest if paid in full in 6 months for purchases of \$99+.")),
	)
}

private fun testCloseButtonPresent() {
	onView(withId(R.id.ModalCloseButton)).check(
		matches(ViewMatchers.isDisplayed()),
	)
}

private fun testTerms() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(
			DriverAtoms.getText(),
			containsString("Terms apply for each option. Offer availability may depend on consumer & merchant eligibility."),
		),
	)
}

private fun clickTileByIndex(index: Int) {
	onWebView(ViewMatchers.withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.CSS_SELECTOR, ".tile:nth-of-type($index)"))
		.perform(DriverAtoms.webClick())
}

private fun clickTilePi4() {
	clickTileByIndex(1)
}

private fun clickTilePayMonthly() {
	clickTileByIndex(2)
}

private fun clickTileNI() {
	clickTileByIndex(3)
}

private fun testDisclosure() {
	onWebView(ViewMatchers.withId(R.id.ModalWebView)).withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body")).check(
		WebViewAssertions.webMatches(DriverAtoms.getText(), containsString("Find more disclosures")),
	)
}

private fun clickSeeOtherModalOptions() {
	onWebView(
		ViewMatchers.withId(R.id.ModalWebView),
	).withElement(DriverAtoms.findElement(Locator.ID, "productListLink")).perform(DriverAtoms.webClick())
}

private fun testModalContent(expectedText: String) {
	onWebView(ViewMatchers.withId(R.id.ModalWebView))
		.withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body"))
		.check(WebViewAssertions.webMatches(DriverAtoms.getText(), containsString(expectedText)))
}

private fun testPi4ModalContent() {
	testModalContent("Pay in 4")
}

private fun testPayMonthlyContent() {
	testModalContent("Pay Monthly")
}

private fun testNIContent() {
	testModalContent("Credit")
}

private fun closeModal() {
	onView(withId(R.id.ModalCloseButton)).perform(click())
}

private fun clickOffer(offerId: Int) {
	onView(withId(offerId)).perform(click())
}

private fun clickShortTermOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerShortTerm)
}

private fun clickLongTermOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerLongTerm)
}

private fun clickPayIn1() {
	clickOffer(com.paypal.messagesdemo.R.id.offerPayIn1)
}

private fun clickNIOffer() {
	clickOffer(com.paypal.messagesdemo.R.id.offerCredit)
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

	@Test
	fun testGenericMessage() {
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
	fun testGenericMessageAndModal() {
		onView(isRoot()).perform(waitFor(500))

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		testModalContent("Pay Later options")
	}

	@Test
	fun testGenericModalNavigatingTiles() {
		onView(isRoot()).perform(waitFor(500))

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		testCloseButtonPresent()
		testPi4Present()
		testPayMonthlyPresent()
		testNIPresent()

		clickTileNI()
		testNIContent()
		clickSeeOtherModalOptions()

		clickTilePi4()
		testPi4ModalContent()
		clickSeeOtherModalOptions()

		clickTilePayMonthly()
		testPayMonthlyContent()
		clickSeeOtherModalOptions()

		closeModal()
	}

	@Test
	fun testGenericModalClose() {
		onView(isRoot()).perform(waitFor(500))

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeModal()
	}

	@Test
	fun testGenericModalCloseAndOpenSameMessage() {
		onView(isRoot()).perform(waitFor(500))

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		clickTilePi4()
		testPi4ModalContent()
		closeModal()

		clickMessage()
		testPi4ModalContent()
	}

	@Test
	fun testShorTermMessage() {
		onView(isRoot()).perform(waitFor(500))
		clickShortTermOffer()
		submit()
		onView(isRoot()).perform(waitFor(500))
	}

	@Test
	fun testShortTermNonQualifyingMessage() {
		onView(isRoot()).perform(waitFor(500))
		clickShortTermOffer()

		onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(typeText("15"))
		submit()
		onView(isRoot()).perform(waitFor(500))
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))
		onView(isRoot()).perform(waitFor(500))
		onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(clearText())
		onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(typeText("2000"))
		submit()
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))

		onView(isRoot()).perform(waitFor(500))
	}

	@Test
	fun testShortTermQualifyingMessage() {
		onView(isRoot()).perform(waitFor(500))
		clickShortTermOffer()

		onView(withId(com.paypal.messagesdemo.R.id.amount)).perform(typeText("1000"))
		submit()
		onView(isRoot()).perform(waitFor(500))
		onView(withId(R.id.content)).check(matches(withText(containsString("250"))))
	}

	@Test
	fun testShortTermMessageAndModal() {
		onView(isRoot()).perform(waitFor(500))
		clickShortTermOffer()
		submit()
		onView(isRoot()).perform(waitFor(500))
		clickMessage()
		testPi4ModalContent()
		closeModal()
	}

	// Demo inputs
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
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.RIGHT)))
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

	@Test
	fun testGenericMonochromeBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleMonochrome)).perform(click())
		submit()

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.MONOCHROME.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}

	@Test
	fun testGenericGrayscaleBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))
		onView(withId(Demo.id.styleGrayscale)).perform(click())
		submit()

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.GRAYSCALE.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class InlineXmlTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(BasicXmlActivity::class.java)

	@Test
	fun testGenericMessage() {
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
	fun testGenericModalCloseWithBackButton() {
		onView(isRoot()).perform(waitFor(500))

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		testModalContent("Pay Later options")
		Espresso.pressBack()
		clickMessage()
		onView(isRoot()).perform(waitFor(5000))
		testModalContent("Pay Later options")

		closeModal()
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class CheckJetPackTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(JetpackActivity::class.java)

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
}
