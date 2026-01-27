package com.example.disastermanager.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.disastermanager.pages.HomePage
import com.example.disastermanager.pages.MapPage
import com.example.disastermanager.pages.ProfilePage
import com.example.disastermanager.pages.WeatherPage


@Composable
fun homeScreen(modifier: Modifier = Modifier, navController: NavController) {
    val navItemList = listOf(
        NavItem(label = "Home", icon = Icons.Default.Home),
        NavItem(label = "Weather", icon = Icons.Default.DateRange),
        NavItem(label = "Map", icon = Icons.Default.Place),
        NavItem(label = "Profile", icon = Icons.Default.Person),
    )
    var selectedIndex by rememberSaveable  { mutableStateOf(0) }
    Scaffold (
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed{index, navItem ->
                    NavigationBarItem(
                        selected =index==selectedIndex,
                        onClick = {
                            selectedIndex=index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                        },
                        label = {
                            Text(text= navItem.label)
                        }
                    )
                }
            }
        }
    ){
        ContentScreen(modifier = modifier.padding(it),selectedIndex)

    }
}
@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> HomePage(modifier)
        1 -> WeatherPage(modifier)
        2 -> MapPage(modifier)
        3 -> ProfilePage(modifier)
    }
}

data class NavItem(
    val label:String,
    val icon: ImageVector
)