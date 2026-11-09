package com.couplefit.widget

import com.couplefit.data.local.HealthDao
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.PartnerRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun healthDao(): HealthDao
    fun partnerRepository(): PartnerRepository
    fun authManager(): FirebaseAuthManager
}
