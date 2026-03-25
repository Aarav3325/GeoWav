package com.aarav.geowav.di

import android.content.Context
import android.content.SharedPreferences
import com.aarav.geowav.data.datasource.retrofit.MessageAPI
import com.aarav.geowav.data.datasource.retrofit.RoadsApi
import com.aarav.geowav.platform.GeofenceHelper
import com.aarav.geowav.platform.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WhatsAppRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RoadsRetrofit

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {


    @Provides
    @Singleton
    fun providePlacesClient(
        @ApplicationContext context: Context
    ): PlacesClient {
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context, fusedClient: FusedLocationProviderClient
    ): LocationManager {
        return LocationManager(context, fusedClient)
    }


    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideGeofencingClient(@ApplicationContext context: Context): GeofencingClient {
        return LocationServices.getGeofencingClient(context)
    }

    @Provides
    @Singleton
    fun provideGeofenceHelper(@ApplicationContext context: Context): GeofenceHelper {
        return GeofenceHelper(context)
    }

    private const val BASE_URL = "https://graph.facebook.com/v22.0/890118200844088/"

    @WhatsAppRetrofit
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @RoadsRetrofit
    @Provides
    @Singleton
    fun provideRoadsRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://roads.googleapis.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRoadsApi(@RoadsRetrofit retrofit: Retrofit): RoadsApi {
        return retrofit.create(RoadsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWhatsAppApi(@WhatsAppRetrofit retrofit: Retrofit): MessageAPI {
        return retrofit.create(MessageAPI::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideRoadsApi(retrofit: Retrofit): RoadsApi {
//        return retrofit.create(RoadsApi::class.java)
//    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("geowav", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }

            setConfigSettingsAsync(configSettings)

            setDefaultsAsync(
                mapOf(
                    "app_enabled" to true
                )
            )
        }
    }

}