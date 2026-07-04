# Keep all data classes used by Gson/Retrofit (no @SerializedName annotations used)
-keep class com.nfctags.app.auth.** { *; }
-keep class com.nfctags.app.data.remote.** { *; }

# Keep Room entities
-keep class com.nfctags.app.data.entities.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Tink / error_prone annotations (R8)
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
