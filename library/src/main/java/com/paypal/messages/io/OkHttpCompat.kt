package com.paypal.messages.io

import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.RequestBody

internal object OkHttpCompat {
	fun parseHttpUrl(url: String): HttpUrl {
		return try {
			val get = HttpUrl::class.java.getMethod("get", String::class.java)
			get.invoke(null, url) as HttpUrl
		} catch (_: NoSuchMethodException) {
			val parse = HttpUrl::class.java.getMethod("parse", String::class.java)
			val res = parse.invoke(null, url) as HttpUrl?
			res ?: throw IllegalArgumentException("Invalid URL: $url")
		}
	}

	fun mediaTypeFrom(contentType: String): MediaType {
		return try {
			val get = MediaType::class.java.getMethod("get", String::class.java)
			get.invoke(null, contentType) as MediaType
		} catch (_: NoSuchMethodException) {
			val parse = MediaType::class.java.getMethod("parse", String::class.java)
			val res = parse.invoke(null, contentType) as MediaType?
			res ?: throw IllegalArgumentException("Invalid media type: $contentType")
		}
	}

	fun createRequestBody(json: String, mediaType: MediaType): RequestBody {
		return try {
			val m = RequestBody::class.java.getMethod(
				"create",
				MediaType::class.java,
				String::class.java,
			)
			m.invoke(null, mediaType, json) as RequestBody
		} catch (_: NoSuchMethodException) {
			val m = RequestBody::class.java.getMethod(
				"create",
				MediaType::class.java,
				ByteArray::class.java,
			)
			m.invoke(null, mediaType, json.toByteArray(Charsets.UTF_8)) as RequestBody
		}
	}
}
