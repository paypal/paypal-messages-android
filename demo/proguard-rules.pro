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