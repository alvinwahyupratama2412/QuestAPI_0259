package com.au.pertemuan11.uicontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.au.pertemuan11.uicontroller.route.DestinasiEntry
import com.au.pertemuan11.uicontroller.route.DestinasiHome
import com.au.pertemuan11.uicontroller.view.EntrySiswaScreen
import com.au.pertemuan11.uicontroller.view.HomeScreen
import com.au.pertemuan11.uicontroller.viewmodel.HomeViewModel
import com.au.pertemuan11.uicontroller.viewmodel.PenyediaViewModel

@Composable
fun DataSiswaApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    HostNavigasi(
        navController = navController,
        modifier = modifier
    )
}

@Composable
fun HostNavigasi(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DestinasiHome.route,
        modifier = modifier
    ) {
        composable(DestinasiHome.route) { backStackEntry ->
            val homeViewModel: HomeViewModel = viewModel(factory = PenyediaViewModel.Factory)
            val shouldRefresh by backStackEntry.savedStateHandle
                .getStateFlow("refresh_siswa", false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    homeViewModel.loadSiswa()
                    backStackEntry.savedStateHandle["refresh_siswa"] = false
                }
            }

            HomeScreen(
                navigateToItemEntry = {
                    navController.navigate(DestinasiEntry.route)
                },
                viewModel = homeViewModel
            )
        }

        composable(DestinasiEntry.route) {
            EntrySiswaScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_siswa", true)
                    navController.popBackStack()
                }
            )
        }
    }
}
