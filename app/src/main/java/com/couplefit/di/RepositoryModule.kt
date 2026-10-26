package com.couplefit.di

import com.couplefit.data.repository.HealthRepositoryImpl
import com.couplefit.data.samsunghealth.SamsungHealthRepository
import com.couplefit.data.samsunghealth.SamsungHealthRepositoryImpl
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
    abstract fun bindSamsungHealthRepository(
        samsungHealthRepositoryImpl: SamsungHealthRepositoryImpl
    ): SamsungHealthRepository
}

