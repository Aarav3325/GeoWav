package com.aarav.geowav.di

import android.content.Context
import com.aarav.geowav.data.datasource.room.ConnectionDao
import com.aarav.geowav.data.datasource.room.PlaceDatabase
import com.aarav.geowav.data.datasource.room.PlacesDAO
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