package com.example.shelfie.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class Detail(val productId: Int) : Route

    @Serializable
    data class Search(val query: String) : Route

    @Serializable
    data class Category(val name: String) : Route

    @Serializable
    data class Enquiry(val productId: Int, val productTitle: String) : Route
}
