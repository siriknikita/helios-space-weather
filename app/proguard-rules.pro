# Keep kotlinx.serialization generated serializers and the @Serializable metadata they rely on.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the serializer() lookup for every @Serializable class in this app.
-keepclassmembers class com.helios.spaceweather.** {
    *** Companion;
}
-keepclasseswithmembers class com.helios.spaceweather.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp / Okio standard keeps.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Room generated implementations are already kept by the Room consumer rules; nothing extra needed.
