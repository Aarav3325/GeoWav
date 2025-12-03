package com.aarav.geowav.data.di

import android.content.Context
import com.aarav.geowav.data.room.ConnectionDao
import com.aarav.geowav.data.room.PlaceDatabase
import com.aarav.geowav.data.room.PlacesDAO
import com.aarav.geowav.presentation.map.PlaceVMProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context : Context) : PlaceDatabase{
        return PlaceDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDao(placeDatabase: PlaceDatabase) : PlacesDAO{
        return placeDatabase.placeDao
    }

    @Provides
    @Singleton
    fun provideConnectionDao(placeDatabase: PlaceDatabase): ConnectionDao{
        return placeDatabase.connectionDao
    }
}