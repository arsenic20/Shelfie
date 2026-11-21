package com.example.shelfie.ui.components

/** "men's clothing" -> "Men's Clothing" */
fun String.prettifyCategory(): String =
    split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
