# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/szerling/env/android_sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontshrink
-dontoptimize

-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep class com.j256.** { *; }
-keep enum com.j256.** { *; }
-keep interface com.j256.** { *; }

#dagger
-keepclassmembers,allowobfuscation class * {
        @javax.inject.* *;
        @dagger.* *;
        <init>();
}
-keep class dagger.* { *; }
-keep class javax.inject.* { *; }

#com.fasterxml
-dontwarn com.fasterxml.jackson.databind.ext.DOMSerializer

-keep class com.fasterxml.jackson.** { *; }

#okhttp
-dontwarn com.squareup.okhttp.**

#retrofit
-keep class com.google.gson.** { *; }
-keep class com.google.inject.* { *; }
-keep class org.apache.http.* { *; }
-keep class org.apache.james.mime4j.* { *; }
-keep class retrofit.** { *; }
-dontwarn rx.*
-keep class sun.misc.Unsafe { *; }
-dontwarn okio.**
-dontwarn retrofit.**
