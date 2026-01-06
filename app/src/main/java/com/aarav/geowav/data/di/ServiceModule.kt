package com.aarav.geowav.data.di

import android.content.Context
import android.content.SharedPreferences
import com.aarav.geowav.data.geofence.GeofenceHelper
import com.aarav.geowav.data.location.LocationManager
import com.aarav.geowav.data.retrofit.MessageAPI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        fusedClient: FusedLocationProviderClient
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

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWhatsAppApi(retrofit: Retrofit): MessageAPI {
        return retrofit.create(MessageAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("geowav", Context.MODE_PRIVATE)
    }

}