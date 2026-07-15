package com.barisguneri.odb2_ecuviewer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.baris.feature.gauge.DashboardRoute
import com.barisguneri.odb2_ecuviewer.navigation.Screen.Dashboard

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: Screen,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Dashboard> {
            DashboardRoute(viewModel = hiltViewModel())
        }
    }
}