# PayPal Messages SDK ProGuard Rules
# Keep all PayPal Messages SDK classes
-keep class com.paypal.messages.** { *; }

# Keep Kotlin reflection support
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Keep classes that might be accessed via reflection
-keepclassmembers class * {
    @com.paypal.messages.** *;
}

# Keep model classes that might be serialized/deserialized
-keep class * implements android.os.Parcelable { *; }

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