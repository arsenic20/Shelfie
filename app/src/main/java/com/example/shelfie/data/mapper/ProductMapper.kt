package com.example.shelfie.data.mapper

import com.example.shelfie.data.local.ProductEntity
import com.example.shelfie.data.remote.dto.ProductDetailResponseDto
import com.example.shelfie.data.remote.dto.ProductDto
import com.example.shelfie.domain.model.Product
import com.example.shelfie.domain.model.Rating

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = image,
    rating = Rating(rate = rating.rate, count = rating.count),
)

fun ProductDetailResponseDto.toDomain(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = image,
    rating = Rating(rate = rating.rate, count = rating.count),
)

fun ProductDto.toEntity() = ProductEntity(id, title, price, description, category, image, rating.rate, rating.count)

fun ProductEntity.toDomain() = Product(id, title, price, description, category, imageUrl, Rating(ratingRate, ratingCount))
