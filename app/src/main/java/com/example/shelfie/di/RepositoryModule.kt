package com.example.shelfie.di

import com.example.shelfie.data.repository.FakeEnquiryRepositoryImpl
import com.example.shelfie.data.repository.ProductRepositoryImpl
import com.example.shelfie.domain.repository.EnquiryRepository
import com.example.shelfie.domain.repository.ProductRepository
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
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindEnquiryRepository(impl: FakeEnquiryRepositoryImpl): EnquiryRepository
}
