# =========================================================================
# CoupleFit ProGuard / R8 Optimization & Obfuscation Rules
# =========================================================================

# 1. Jetpack Compose & Material 3
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# 2. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# 3. Health Connect Client
-keep class androidx.health.connect.client.** { *; }
-keep class androidx.health.connect.client.records.** { *; }
-dontwarn androidx.health.connect.**

# 4. Jetpack Glance (AppWidget)
-keep class androidx.glance.** { *; }
-keep class androidx.glance.appwidget.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * implements androidx.glance.appwidget.action.ActionCallback { <init>(); }

# 5. Hilt / Dependency Injection
-keep class * extends dagger.hilt.android.internal.managers.** { *; }
-dontwarn dagger.hilt.**

# 6. Firebase & Google Identity
-keepattributes *Annotation*,InnerClasses,EnclosingMethod
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# 7. Kotlin Coroutines & Serialization
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable class *;
}
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keep class com.couplefit.data.model.** { *; }
-dontwarn kotlinx.serialization.**

# 8. ProfileInstaller
-keep class androidx.profileinstaller.** { *; }
-dontwarn androidx.profileinstaller.**
