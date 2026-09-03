# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep kotlinx.serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}
-keepclassmembers enum * {
    **[] $VALUES;
    public *;
}

# Keep Retrofit and OkHttp classes
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
