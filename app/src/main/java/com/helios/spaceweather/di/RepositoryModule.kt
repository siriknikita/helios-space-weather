package com.helios.spaceweather.di

import com.helios.spaceweather.data.repository.KpRepositoryImpl
import com.helios.spaceweather.domain.repository.KpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds repository implementations to their domain interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindKpRepository(impl: KpRepositoryImpl): KpRepository
}
