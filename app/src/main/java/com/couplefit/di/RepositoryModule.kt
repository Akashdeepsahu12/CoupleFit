package com.couplefit.di

import com.couplefit.data.repository.HealthRepositoryImpl
import com.couplefit.domain.repository.HealthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHealthRepository(
        healthRepositoryImpl: HealthRepositoryImpl
    ): HealthRepository

    @Binds
    abstract fun bindPartnerRepository(
        partnerRepositoryImpl: com.couplefit.data.repository.PartnerRepositoryImpl
    ): com.couplefit.domain.repository.PartnerRepository

    @Binds
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.couplefit.data.repository.SyncRepositoryImpl
    ): com.couplefit.domain.repository.SyncRepository

    @Binds
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: com.couplefit.data.repository.ChallengeRepositoryImpl
    ): com.couplefit.domain.repository.ChallengeRepository
}

