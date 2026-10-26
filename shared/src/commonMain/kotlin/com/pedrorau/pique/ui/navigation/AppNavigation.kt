package com.pedrorau.pique.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.home_section_modalities
import com.pedrorau.pique.resources.home_section_safety
import com.pedrorau.pique.resources.ic_fish
import com.pedrorau.pique.resources.ic_home
import com.pedrorau.pique.resources.ic_knot
import com.pedrorau.pique.resources.nav_home
import com.pedrorau.pique.resources.nav_knots
import com.pedrorau.pique.resources.nav_species
import com.pedrorau.pique.resources.read_water_title
import com.pedrorau.pique.ui.components.ComingSoonScreen
import com.pedrorau.pique.ui.home.HomeScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

object Routes {
    const val HOME = "home"
    const val KNOTS = "knots"
    const val SPECIES = "species"
    const val READ_WATER = "readWater"
    const val MODALITIES = "modalities"
    const val SAFETY = "safety"
}

private data class TopLevelDestination(
    val route: String,
    val label: StringResource,
    val icon: DrawableResource
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, Res.string.nav_home, Res.drawable.ic_home),
    TopLevelDestination(Routes.KNOTS, Res.string.nav_knots, Res.drawable.ic_knot),
    TopLevelDestination(Routes.SPECIES, Res.string.nav_species, Res.drawable.ic_fish)
)

// Standard bottom-bar behavior: one copy of each tab on the back stack, each tab keeps its state.
private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PiqueBottomBar(
                    currentRoute = currentRoute,
                    onSelect = { navController.navigateToTopLevel(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenReadWater = { navController.navigate(Routes.READ_WATER) },
                    onOpenKnots = { navController.navigateToTopLevel(Routes.KNOTS) },
                    onOpenModalities = { navController.navigate(Routes.MODALITIES) },
                    onOpenSpecies = { navController.navigateToTopLevel(Routes.SPECIES) },
                    onOpenSafety = { navController.navigate(Routes.SAFETY) }
                )
            }
            composable(Routes.KNOTS) {
                ComingSoonScreen(title = stringResource(Res.string.nav_knots))
            }
            composable(Routes.SPECIES) {
                ComingSoonScreen(title = stringResource(Res.string.nav_species))
            }
            composable(Routes.READ_WATER) {
                ComingSoonScreen(
                    title = stringResource(Res.string.read_water_title),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MODALITIES) {
                ComingSoonScreen(
                    title = stringResource(Res.string.home_section_modalities),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SAFETY) {
                ComingSoonScreen(
                    title = stringResource(Res.string.home_section_safety),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun PiqueBottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        topLevelDestinations.forEach { destination ->
            val selected = destination.route == currentRoute
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(destination.route) },
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(destination.label)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
