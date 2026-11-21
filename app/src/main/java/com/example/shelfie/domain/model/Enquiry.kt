package com.example.shelfie.domain.model

data class Enquiry(
    val productId: Int,
    val productTitle: String,
    val name: String,
    val email: String,
    val phone: String?,
    val message: String,
)
