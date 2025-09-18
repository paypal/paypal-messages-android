# PayPal Messages SDK ProGuard Rules
# Keep all PayPal Messages SDK classes
-keep class com.paypal.messages.** { *; }
-keepnames class com.paypal.messages.** { *; }

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

# Keep attributes needed for Kotlin reflection
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile, LineNumberTable
-keepattributes Signature, Exceptions

# OkHttp Conflict Resolution
# This prevents conflicts when the consuming app has a different version of OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
# Do not attempt to repackage/relocate OkHttp at consumer shrink time.
# If relocation is required, shade at build time in the library instead.
# Keep any OkHttp 4.x Companion objects and extension functions
-keep class okhttp3.HttpUrl$Companion { *; }
-keep class okhttp3.MediaType$Companion { *; }
-keep class okhttp3.RequestBody$Companion { *; }
-keep class okhttp3.ResponseBody$Companion { *; }
