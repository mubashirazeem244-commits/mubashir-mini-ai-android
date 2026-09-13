# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

# Keep Hilt
-keep class dagger.hilt.internal.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
