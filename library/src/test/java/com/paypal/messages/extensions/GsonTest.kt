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
		assertTrue(number is Number, "number is not Number")
	}

	@Test
	fun testJsonValueToAnyWithBoolean() {
		val parsedJson = JsonParser.parseString("true")
		val boolean = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(boolean is Boolean, "boolean is not Boolean")
	}

	@Test
	fun testJsonValueToAnyWithString() {
		val parsedJson = JsonParser.parseString("string")
		val string = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(string is String, "string is not String")
	}

	@Test
	fun testJsonValueToAnyWithArray() {
		val parsedJson = JsonParser.parseString("[1]")
		val array = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(array is JsonArray, "array is not JsonArray")
	}

	@Test
	fun testJsonValueToAnyWithObject() {
		val parsedJson = JsonParser.parseString("{}")
		val map = Gson::class.jsonValueToAny(parsedJson)
		assertTrue(map is MutableMap<*, *>, "map is not MutableMap<*, *>")
	}

	@Test
	fun testJsonValueToAnyWithFailure() {
		val parsedJson = JsonParser.parseString("null")
		assertThrows<IllegalArgumentException> { Gson::class.jsonValueToAny(parsedJson) }
	}

	private val gson = Gson()

	@Test
	fun testGetJsonObject() {
		data class TestClass(val testKey: String)
		val jsonObject = gson.getJsonObject(TestClass("test_value"))
		assertEquals(jsonObject.get("testKey").asString, "test_value")
	}

	@Test
	fun testGetJsonElement() {
		val jsonElement = gson.getJsonElement("test_value")
		assertEquals(jsonElement.asString, "test_value")
	}
}
