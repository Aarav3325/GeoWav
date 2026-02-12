package com.aarav.geowav.di

import com.aarav.geowav.data.repository.CircleRepositoryImpl
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.LiveLocationSharingRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGeoActivityRepository(
        impl: GeoActivityRepositoryImpl
    ): GeoActivityRepository

    @Binds
    abstract fun bindPlacesRepository(
        impl: PlaceRepositoryImpl
    ): PlaceRepository

    @Binds
    abstract fun bindLiveLocationSharingRepository(
        liveLocationSharingRepositoryImpl: LiveLocationSharingRepositoryImpl
    ): LiveLocationSharingRepository

    @Binds
    abstract fun bindLiveCircleRepository(
        circleRepositoryImpl: CircleRepositoryImpl
    ): CircleRepository
}