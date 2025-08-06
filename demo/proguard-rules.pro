# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep PayPal Messages SDK classes
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

# Keep references to the library from the demo app
-dontwarn com.paypal.messages.**

# Keep attributes needed for Kotlin reflection
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile, LineNumberTable
-keepattributes Signature, Exceptions

# OkHttp Platform used when running on Java 8 or below
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepattributes Signature

# Keep Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }

# Preserve the special static methods that are required in all enumeration classes
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}