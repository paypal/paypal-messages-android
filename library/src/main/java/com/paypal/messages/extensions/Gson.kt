package com.paypal.messages.extensions

import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlin.reflect.KClass

fun KClass<Gson>.jsonElementToMutableMap(jsonElement: JsonElement): MutableMap<String, Any> {
	val mutableMap = mutableMapOf<String, Any>()

	if (jsonElement.isJsonObject) {
		val jsonObject = jsonElement.asJsonObject
		for ((key, value) in jsonObject.entrySet()) {
			mutableMap[key] = jsonValueToAny(value)
		}
	}

	return mutableMap
}

fun KClass<Gson>.jsonValueToAny(jsonElement: JsonElement): Any {
	return when {
		jsonElement.isJsonPrimitive -> {
			val primitive = jsonElement.asJsonPrimitive
			when {
				primitive.isBoolean -> primitive.asBoolean
				primitive.isNumber -> primitive.asNumber
				else -> primitive.asString
			}
		}
		jsonElement.isJsonArray -> jsonElement.asJsonArray
		jsonElement.isJsonObject -> jsonElementToMutableMap(jsonElement)
		else -> throw IllegalArgumentException("Unsupported JSON element type: ${jsonElement::class.java.simpleName}")
	}
}
