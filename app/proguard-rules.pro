# youtubedl-android / yt-dlp
-keep class com.yausername.** { *; }
-keep class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.**
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.nazil.app.**$$serializer { *; }
-keepclassmembers class com.nazil.app.** { *** Companion; }
-keepclasseswithmembers class com.nazil.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Compose
-dontwarn androidx.compose.**
