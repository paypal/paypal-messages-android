package com.paypal.messages.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for OkHttpCompat utility class
 * Tests compatibility layer between OkHttp 3.x and 4.x
 */
class OkHttpCompatTest {

	@Test
	fun testParseHttpUrlWithValidUrl() {
		// Test with a valid HTTPS URL
		val url = "https://www.paypal.com/sdk/js?client-id=test"
		val httpUrl = OkHttpCompat.parseHttpUrl(url)

		assertNotNull(httpUrl)
		assertEquals("https", httpUrl.scheme)
		assertEquals("www.paypal.com", httpUrl.host)
		assertTrue(httpUrl.toString().contains("client-id=test"))
	}

	@Test
	fun testParseHttpUrlWithSimpleUrl() {
		// Test with a simple HTTP URL
		val url = "http://example.com"
		val httpUrl = OkHttpCompat.parseHttpUrl(url)

		assertNotNull(httpUrl)
		assertEquals("http", httpUrl.scheme)
		assertEquals("example.com", httpUrl.host)
	}

	@Test
	fun testParseHttpUrlWithPath() {
		// Test with URL containing path
		val url = "https://api.paypal.com/v1/messages"
		val httpUrl = OkHttpCompat.parseHttpUrl(url)

		assertNotNull(httpUrl)
		assertEquals("/v1/messages", httpUrl.encodedPath)
	}

	@Test
	fun testParseHttpUrlWithInvalidUrl() {
		// Test with an invalid URL should throw exception
		val invalidUrl = "not a valid url"
		
		try {
			OkHttpCompat.parseHttpUrl(invalidUrl)
			// Should not reach here
			assertTrue(false, "Expected an exception to be thrown")
		} catch (e: Exception) {
			// Expected - either IllegalArgumentException or a wrapped exception
			assertTrue(e is IllegalArgumentException || e.cause is IllegalArgumentException)
		}
	}

	@Test
	fun testMediaTypeFromWithValidContentType() {
		// Test with valid JSON content type
		val contentType = "application/json; charset=utf-8"
		val mediaType = OkHttpCompat.mediaTypeFrom(contentType)

		assertNotNull(mediaType)
		assertEquals("application", mediaType.type)
		assertEquals("json", mediaType.subtype)
	}

	@Test
	fun testMediaTypeFromWithSimpleContentType() {
		// Test with simple content type
		val contentType = "text/plain"
		val mediaType = OkHttpCompat.mediaTypeFrom(contentType)

		assertNotNull(mediaType)
		assertEquals("text", mediaType.type)
		assertEquals("plain", mediaType.subtype)
	}

	@Test
	fun testMediaTypeFromWithXmlContentType() {
		// Test with XML content type
		val contentType = "application/xml"
		val mediaType = OkHttpCompat.mediaTypeFrom(contentType)

		assertNotNull(mediaType)
		assertEquals("application", mediaType.type)
		assertEquals("xml", mediaType.subtype)
	}

	@Test
	fun testMediaTypeFromWithInvalidContentType() {
		// Test with invalid content type should throw exception
		val invalidContentType = "not a valid content type"
		
		try {
			OkHttpCompat.mediaTypeFrom(invalidContentType)
			// Should not reach here
			assertTrue(false, "Expected an exception to be thrown")
		} catch (e: Exception) {
			// Expected - either IllegalArgumentException or a wrapped exception
			assertTrue(e is IllegalArgumentException || e.cause is IllegalArgumentException)
		}
	}

	@Test
	fun testCreateRequestBodyWithJsonString() {
		// Test creating a request body with JSON content
		val json = """{"clientId":"test123","amount":100.0}"""
		val mediaType = OkHttpCompat.mediaTypeFrom("application/json")
		val requestBody = OkHttpCompat.createRequestBody(json, mediaType)

		assertNotNull(requestBody)
		assertNotNull(requestBody.contentType())
		// Content length should match the JSON string byte length
		assertEquals(json.toByteArray().size.toLong(), requestBody.contentLength())
	}

	@Test
	fun testCreateRequestBodyWithEmptyString() {
		// Test creating a request body with empty content
		val emptyJson = ""
		val mediaType = OkHttpCompat.mediaTypeFrom("application/json")
		val requestBody = OkHttpCompat.createRequestBody(emptyJson, mediaType)

		assertNotNull(requestBody)
		assertNotNull(requestBody.contentType())
		assertEquals(0L, requestBody.contentLength())
	}

	@Test
	fun testCreateRequestBodyWithUtf8Content() {
		// Test creating a request body with UTF-8 special characters
		val jsonWithUtf8 = """{"message":"Hello 世界 🌍"}"""
		val mediaType = OkHttpCompat.mediaTypeFrom("application/json; charset=utf-8")
		val requestBody = OkHttpCompat.createRequestBody(jsonWithUtf8, mediaType)

		assertNotNull(requestBody)
		assertNotNull(requestBody.contentType())
		// UTF-8 characters take more bytes than string length
		assertEquals(jsonWithUtf8.toByteArray(Charsets.UTF_8).size.toLong(), requestBody.contentLength())
		assertTrue(requestBody.contentLength() > jsonWithUtf8.length.toLong())
	}

	@Test
	fun testCreateRequestBodyWithLargePayload() {
		// Test creating a request body with a larger payload
		val largeJson = """{"data":"${"x".repeat(1000)}"}"""
		val mediaType = OkHttpCompat.mediaTypeFrom("application/json")
		val requestBody = OkHttpCompat.createRequestBody(largeJson, mediaType)

		assertNotNull(requestBody)
		assertNotNull(requestBody.contentType())
		assertEquals(largeJson.toByteArray().size.toLong(), requestBody.contentLength())
		assertTrue(requestBody.contentLength() > 1000L)
	}
}
