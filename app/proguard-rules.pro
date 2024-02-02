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
# [s] Common Setting ==============================================================================
# 소스 파일, 라인정보 유지
-keepattributes SourceFile,LineNumberTable

-keep class com.nh.cowauction.model.** { *; }
-keepclassmembers class com.nh.cowauction.model.** { *;}

# Remove Log Disable
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int e(...);
}
# [e] Common Setting ==============================================================================
# [s] Retrofit Proguard ===========================================================================
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
# [e] Retrofit Proguard ===========================================================================

# [s] Gson Proguard ==============================================================================
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# [e] Gson Proguard ==============================================================================

# [s] Glide Proguard ==============================================================================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder.InternalRewinder {
    *** rewind();
}
#-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
#  *** rewind();
#}

# for DexGuard only Error
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
# [e] Glide Proguard ==============================================================================

#[s] Kotlinx Serialization ========================================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
#-keep,includedescriptorclasses class com.nh.cowauction.model.**$$serializer { *; } # <-- change package name to your app's
#-keepclassmembers class com.nh.cowauction.model.** { # <-- change package name to your app's
#    *** Companion;
#}
#-keepclasseswithmembers class com.nh.cowauction.model** { # <-- change package name to your app's
#    kotlinx.serialization.KSerializer serializer(...);
#}
#[e] Kotlinx Serialization ===================================================================
#[s] Netty Client
-dontwarn io.netty.**
-keep class io.netty.** { *; }
-keep interface io.netty.** { *; }
#[e] Netty Client
#[s] Kakao Live ==============================================================================
-keepattributes SourceFile,LineNumberTable, InnerClasses, Exceptions, Signature
-keepattributes *Annotation*
-dontoptimize
-dontwarn org.**
-dontwarn retrofit2.**

-keep class io.agora.**{*;}
-keep class org.** { *;}
-keep interface org.** { *;}

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }
#[e] Kakao Live ==============================================================================