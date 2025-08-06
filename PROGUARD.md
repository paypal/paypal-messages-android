# ProGuard Rules for PayPal Messages Android SDK

This document outlines the ProGuard rules required for proper operation of the PayPal Messages Android SDK and its dependencies.

## Core Library Dependencies

The PayPal Messages Android SDK relies on several key dependencies that require specific ProGuard rules:

1. **OkHttp3** - Used for network communication
2. **Gson** - Used for JSON serialization/deserialization  
3. **Kotlin Coroutines** - Used for asynchronous operations

## Required ProGuard Rules

### PayPal Messages SDK Rules

```proguard
# Keep all PayPal Messages SDK classes
-keep class com.paypal.messages.** { *; }
-keepnames class com.paypal.messages.** { *; }

# Keep classes that might be accessed via reflection
-keepclassmembers class * {
    @com.paypal.messages.** *;
}

# Keep references to the library from client apps
-dontwarn com.paypal.messages.**
```

### Kotlin Rules

```proguard
# Keep Kotlin reflection support
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Keep attributes needed for Kotlin reflection
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile, LineNumberTable
-keepattributes Signature, Exceptions

# Keep Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
```

### OkHttp Rules

```proguard
# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# OkHttp Platform used when running on Java 8 or below
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
```

### Gson Rules

```proguard
# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepattributes Signature
```

### General Rules

```proguard
# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep model classes that might be serialized/deserialized
-keep class * implements android.os.Parcelable { *; }
```

## Verifying ProGuard Rules

To verify that the ProGuard rules are working correctly:

1. Build your app with minification enabled
2. Test all functionality related to PayPal Messages
3. Check for any `ClassNotFoundException` or `NoSuchMethodError` exceptions

The demo app includes a test build variant with minification enabled (`debugWithMinifyEnabled`) and a test activity that exercises all the key libraries to verify the ProGuard rules.

## Troubleshooting

If you encounter issues with ProGuard:

1. Check logcat for specific class or method not found errors
2. Add specific keep rules for the missing classes
3. Use the `-verbose` option with ProGuard to get more information
4. Consider using `-dontobfuscate` during debugging to isolate minification issues

## Additional Resources

- [Android Developer documentation on shrinking code](https://developer.android.com/studio/build/shrink-code)
- [ProGuard manual](https://www.guardsquare.com/manual/configuration/usage)