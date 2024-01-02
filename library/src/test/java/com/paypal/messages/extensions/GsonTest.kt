package com.paypal.messages.extensions

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GsonTest {
	@Test
	fun testJsonElementToMutableMap() {
		val json = JsonParser.parseString(
			"""{"number": 1, "boolean": true, "string": "string", "array": [1]}""",
		)
		val map = Gson::class.jsonElementToMutableMap(json)
		assertEquals(JsonParser.parseString("1").asNumber, map["number"])
		assertEquals(JsonParser.parseString("true").asBoolean, map["boolean"])
		assertEquals(JsonParser.parseString("string").asString, map["string"])
		assertEquals(JsonParser.parseString("[1]").asJsonArray, map["array"])
	}

	@Test
	fun testJsonElementToMutableMapNotAnObject() {
		val json = JsonParser.parseString("")
		val map = Gson::class.jsonElementToMutableMap(json)
		assertEquals(0, map.size)
	}

	@Test
	fun testJsonValueToAnyWithNumber() {
		val parsedJson = JsonParser.parseString("1")
		val number = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(number is Number)
	}

	@Test
	fun testJsonValueToAnyWithBoolean() {
		val parsedJson = JsonParser.parseString("true")
		val boolean = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(boolean is Boolean)
	}

	@Test
	fun testJsonValueToAnyWithString() {
		val parsedJson = JsonParser.parseString("string")
		val string = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(string is String)
	}

	@Test
	fun testJsonValueToAnyWithArray() {
		val parsedJson = JsonParser.parseString("[1]")
		val array = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(array is JsonArray)
	}

	@Test
	fun testJsonValueToAnyWithObject() {
		val parsedJson = JsonParser.parseString("{}")
		val map = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(map is MutableMap<*, *>)
	}

	@Test
	fun testJsonValueToAnyWithFailure() {
		val parsedJson = JsonParser.parseString("null")
		assertThrows<IllegalArgumentException> { Gson::class.jsonValueToAny(parsedJson) }
	}
}
