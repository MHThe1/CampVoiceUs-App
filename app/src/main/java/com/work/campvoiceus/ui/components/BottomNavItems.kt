package com.work.campvoiceus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import com.work.campvoiceus.models.BottomNavItem

val bottomNavItems = listOf(
    BottomNavItem(route = "home", icon = Icons.Filled.Home, label = "Home"),
    BottomNavItem(route = "createThread", icon = Icons.Filled.AddCircle, label = "Create Thread"),
    BottomNavItem(route = "notifications", icon = Icons.Filled.Notifications, label = "Notifications"),
    BottomNavItem(route = "profile", icon = Icons.Filled.Person, label = "Profile")
)
