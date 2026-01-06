package com.aarav.geowav.data.di

import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.domain.repository.GeoActivityRepository
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
}