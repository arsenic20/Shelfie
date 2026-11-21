package com.example.shelfie.data.remote.dto

import kotlinx.serialization.Serializable

/** Response of GET /products/{id}. */
@Serializable
data class ProductDetailResponseDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: RatingDto,
)
