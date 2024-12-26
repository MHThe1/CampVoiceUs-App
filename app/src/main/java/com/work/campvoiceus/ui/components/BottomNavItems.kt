package com.work.campvoiceus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CommentBank
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Preview
import com.work.campvoiceus.models.BottomNavItem

val bottomNavItems = listOf(
    BottomNavItem(route = "home", icon = Icons.Filled.Home, label = "Home"),
    BottomNavItem(route = "createThread", icon = Icons.Filled.AddCircle, label = "Create Thread"),
    BottomNavItem(route = "profile", icon = Icons.Filled.Person, label = "Profile"),
    BottomNavItem(route = "threadDetails/6767dbea2fb1563f0a3b416b", icon = Icons.Filled.CommentBank, label= "Thread"),
    BottomNavItem(route = "authorProfile/67659b4504a371bfe3cec11f", icon = Icons.Filled.Preview, label= "Thread")

)
