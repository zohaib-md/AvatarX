package com.project.avatarx.di

import com.project.avatarx.data.repository.GarmentRepositoryImpl
import com.project.avatarx.data.repository.MeasurementRepositoryImpl
import com.project.avatarx.data.repository.PoseRepositoryImpl
import com.project.avatarx.domain.repository.GarmentRepository
import com.project.avatarx.domain.repository.MeasurementRepository
import com.project.avatarx.domain.repository.PoseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPoseRepository(impl: PoseRepositoryImpl): PoseRepository

    @Binds
    @Singleton
    abstract fun bindMeasurementRepository(impl: MeasurementRepositoryImpl): MeasurementRepository

    @Binds
    @Singleton
    abstract fun bindGarmentRepository(impl: GarmentRepositoryImpl): GarmentRepository
}
