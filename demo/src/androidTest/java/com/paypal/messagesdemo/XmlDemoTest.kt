package com.paypal.messagesdemo

import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messagesdemo.R as Demo

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

	@Test
	fun testGenericMessage() {
		// Perform a delay
		waitForApp(5000)

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
		waitForApp(2000)

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
		waitForApp(2000)

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
		waitForApp(5000)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeModal()
	}

	@Test
	fun testGenericModalCloseAndOpenSameMessage() {
		waitForApp(5000)

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
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
	}

	@Test
	fun testShortTermNonQualifyingMessage() {
		waitForApp(500)
		clickShortTermOffer()

		typeAmount("15")
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))
		waitForApp(500)
		clearAmount()
		typeAmount("2000")
		submit()
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))

		waitForApp(500)
	}

	@Test
	fun testShortTermQualifyingMessage() {
		waitForApp(500)
		clickShortTermOffer()

		typeAmount("1000")
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("250"))))
// 		checkMessage("250")
// 		waitForApp(5000)
	}

	@Test
	fun testShortTermMessageAndModal() {
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
		closeModal()
	}

	@Test
	fun testShortTermOpenAndSwitchModal() {
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
		clickSeeOtherModalOptions()
		waitForApp(200)
		clickPi4Tile()
		checkPi4ModalContent()
	}

	@Test
	fun testShortTermQualifyingModal() {
		waitForApp(500)
		clickShortTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
	}

	@Test
	fun testLongTermNonQualifyingMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("15")
		submit()
		waitForApp(500)
		checkMessage("%paypal_logo% Pay monthly")
		clearAmount()
		typeAmount("20000")
		submit()
		checkMessage("%paypal_logo% Pay monthly")
	}

	@Test
	fun testLongTermQualifyingMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(500)
		checkMessage("$95.55")
	}

	@Test
	fun testLongTermNoAmountMessage() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("Enter an amount of")
	}

	@Test
	fun testLongTermQualifyingAmountMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("12 months")
	}

	@Test
	fun testLongTermRangeMessage() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()

		typeCalculatorAmount("1000")
		waitForApp(5000)
		modalContent("for 12")

		clearCalculatorAmount()
		waitForApp(500)
		typeCalculatorAmount("100")
		waitForApp(5000)
		modalContent("Enter an amount")

		clearCalculatorAmount()
		typeCalculatorAmount("20000")
		waitForApp(5000)
		modalContent("Enter an amount no larger")
		waitForApp(500)
	}

	@Test
	fun testLongTermModalNavigation() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()

		clickSeeOtherModalOptions()
		clickPayMonthlyTile()
		checkPayMonthlyContent()
	}

	@Test
	fun testLongTermModalDisclosure() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()
		waitForApp(5000)

		onWebView().forceJavascriptEnabled()

		modalContent("Find more disclosures")

// 		clickDisclosure()
// 		onView(isRoot()).perform(waitForApp(50000))

// 		pressBack()
// 		waitForApp(5000)

// 		checkPayMonthlyContent()
	}

	@Test
	fun testNiMessage() {
		waitForApp(500)
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)

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
// 		waitForApp(500)
// 		waitForApp(500)
// 		clickNIOffer()
// 		submit()
// 		waitForApp(1000)
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
		waitForApp(500)

		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.BLACK.colorResId)
		}

		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}

	@Test
	fun testAlignment() {
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		onView(withId(Demo.id.styleCenter)).perform(click())
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.CENTER.value)))

		onView(withId(Demo.id.styleRight)).perform(click())
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.RIGHT)))
	}

	@Test
	fun testMessageColors() {
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)

		onView(withId(Demo.id.styleWhite)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.WHITE)

		onView(withId(Demo.id.styleMonochrome)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.MONOCHROME)

		onView(withId(Demo.id.styleGrayscale)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.GRAYSCALE)

		onView(withId(Demo.id.styleBlack)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)
	}

	@Test
	fun testLogoAlignment() {
		waitForApp(2000)
		onView(withId(Demo.id.styleInline)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("Buy now, pay later with %paypal_logo%. Learn more")

		onView(withId(Demo.id.styleAlternative)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")

		onView(withId(Demo.id.styleNone)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("Buy now, pay later with PayPal. Learn more")

		onView(withId(Demo.id.stylePrimary)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		waitForApp(1000)
	}

	// @Test
	// fun testCrossBorder(){
	// 	activityScenarioRule.scenario.onActivity { activity ->
	// 		activity.environment = PayPalEnvironment.stage(("msmaster.qa.paypal.com"))
	// 	}

	// 	waitForApp(50000)
	// 	onView(withId(Demo.id.styleAlternative)).perform(click())
	// 	submit()
	// 	checkMessage("%paypal_logo% Buy now, pay later. Learn more")
	// 	waitForApp(5000)

	// }

// 	@Test
// 	fun testGenericInlineLogoBuyNowPayLaterMessage() {
// 		// Perform a delay
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
// 		waitForApp(500)
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
