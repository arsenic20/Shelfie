package com.example.shelfie.data.mapper

import com.example.shelfie.data.remote.dto.ProductDto
import com.example.shelfie.data.remote.dto.RatingDto
import com.example.shelfie.domain.model.Rating
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductMapperTest {

    @Test
    fun `maps dto fields to domain product`() {
        val dto = ProductDto(
            id = 7,
            title = "Backpack",
            price = 109.95,
            description = "Fits 15 inch laptops",
            category = "men's clothing",
            image = "https://example.com/img.jpg",
            rating = RatingDto(rate = 3.9, count = 120),
        )

        val product = dto.toDomain()

        assertEquals(7, product.id)
        assertEquals("Backpack", product.title)
        assertEquals(109.95, product.price, 0.0)
        assertEquals("Fits 15 inch laptops", product.description)
        assertEquals("men's clothing", product.category)
        assertEquals("https://example.com/img.jpg", product.imageUrl)
        assertEquals(Rating(3.9, 120), product.rating)
    }
}
