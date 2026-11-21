package com.example.shelfie.data.remote.api

import com.example.shelfie.data.remote.dto.ProductDetailResponseDto
import com.example.shelfie.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApi {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDetailResponseDto

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<ProductDto>

    @GET("products/categories")
    suspend fun getCategories(): List<String>
}
