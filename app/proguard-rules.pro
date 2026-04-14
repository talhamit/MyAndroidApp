# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.speakmate.app.data.model.** { *; }
# Keep Retrofit models
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
