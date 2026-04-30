# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities and DAOs
-keep class com.cityfix.data.local.entity.** { *; }
-keep class com.cityfix.data.local.dao.** { *; }

# Keep domain models
-keep class com.cityfix.domain.model.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# DataStore
-keep class androidx.datastore.** { *; }

# Coil
-keep class coil.** { *; }
