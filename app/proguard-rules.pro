# ProGuard rules for CodeArena security and optimization

# Gson serialization/deserialization models
-keepattributes *Annotation*, Signature, InnerClasses
-keepclassmembers class com.codearena.data.model.** { *; }
-keep class com.codearena.data.model.** { *; }

# Room Database entities and DAOs
-keep class com.codearena.data.db.** { *; }
-keepclassmembers class com.codearena.data.db.** { *; }

# Retrofit interfaces & models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.codearena.data.api.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# OkHttp optional platforms
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
