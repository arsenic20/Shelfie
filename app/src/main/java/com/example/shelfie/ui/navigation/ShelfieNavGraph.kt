package com.example.shelfie.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shelfie.ui.detail.ProductDetailScreen
import com.example.shelfie.ui.enquiry.EnquiryScreen
import com.example.shelfie.ui.category.CategoryScreen
import com.example.shelfie.ui.search.SearchScreen
import com.example.shelfie.ui.home.HomeScreen

@Composable
fun ShelfieNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,
    ) {
        composable<Route.Home> {
            HomeScreen(
                onProductClick = { navController.navigate(Route.Detail(it)) },
                onSearch = { navController.navigate(Route.Search(it)) },
                onCategoryClick = { navController.navigate(Route.Category(it)) },
            )
        }
        composable<Route.Search> {
            SearchScreen(
                onProductClick = { navController.navigate(Route.Detail(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Category> {
            CategoryScreen(
                onProductClick = { navController.navigate(Route.Detail(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Detail> {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onEnquire = { id, title -> navController.navigate(Route.Enquiry(id, title)) },
            )
        }
        composable<Route.Enquiry> {
            EnquiryScreen(onBack = { navController.popBackStack() })
        }
    }
}
