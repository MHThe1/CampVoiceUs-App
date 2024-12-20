package com.work.campvoiceus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.work.campvoiceus.models.BottomNavItem

val bottomNavItems = listOf(
    BottomNavItem(route = "home", icon = Icons.Filled.Home, label = "Home"),
    BottomNavItem(route = "profile", icon = Icons.Filled.Person, label = "Profile")
)