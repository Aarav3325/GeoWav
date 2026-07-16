package com.aarav.geowav.di

import com.aarav.geowav.data.repository.DayReplayRepositoryImpl
import com.aarav.geowav.data.repository.CircleRepositoryImpl
import com.aarav.geowav.data.repository.EmergencySharingRepositoryImpl
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.LiveLocationSharingRepositoryImpl
import com.aarav.geowav.data.repository.LocationPermissionRepositoryImpl
import com.aarav.geowav.data.repository.NotificationRepositoryImpl
import com.aarav.geowav.data.repository.PaymentRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.data.repository.SessionHistoryRepositoryImpl
import com.aarav.geowav.data.repository.SubscriptionRepositoryImpl
import com.aarav.geowav.data.repository.PaywallConfigRepositoryImpl
import com.aarav.geowav.data.repository.ReleaseNotesRepositoryImpl
import com.aarav.geowav.data.repository.ViewerLocationRepositoryImpl
import com.aarav.geowav.domain.repository.DayReplayRepository
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.EmergencySharingRepository
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.LocationPermissionRepository
import com.aarav.geowav.domain.repository.PaymentRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.aarav.geowav.domain.repository.PaywallConfigRepository
import com.aarav.geowav.domain.repository.ReleaseNotesRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
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
    abstract fun bindDayReplayRepository(
        impl: DayReplayRepositoryImpl
    ): DayReplayRepository

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


    @Binds
    abstract fun bindLocationPermissionRepository(
        locationPermissionRepositoryImpl: LocationPermissionRepositoryImpl
    ): LocationPermissionRepository

    @Binds
    abstract fun bindEmergencySharingRepository(
        emergencySharingRepositoryImpl: EmergencySharingRepositoryImpl
    ): EmergencySharingRepository

    @Binds
    abstract fun bindViewerLocationRepository(
        viewerLocationSharingRepositoryImpl: ViewerLocationRepositoryImpl
    ): ViewerLocationRepository

    @Binds
    abstract fun bindSessionHistoryRepository(
        sessionHistoryRepositoryImpl: SessionHistoryRepositoryImpl
    ): SessionHistoryRepository

    @Binds
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    abstract fun bindPaywallConfigRepository(
        paywallConfigRepositoryImpl: PaywallConfigRepositoryImpl
    ): PaywallConfigRepository

    @Binds
    abstract fun bindReleaseNotesRepository(
        releaseNotesRepositoryImpl: ReleaseNotesRepositoryImpl
    ): ReleaseNotesRepository
}