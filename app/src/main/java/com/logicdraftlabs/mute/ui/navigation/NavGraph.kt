package com.logicdraftlabs.mute.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.logicdraftlabs.mute.ui.screens.HomeScreen
import com.logicdraftlabs.mute.ui.screens.ScheduleEditScreen
import com.logicdraftlabs.mute.ui.screens.SchedulesScreen
import com.logicdraftlabs.mute.ui.screens.SettingsScreen
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Schedules : Screen("schedules")
    object ScheduleEdit : Screen("schedule_edit?scheduleId={scheduleId}") {
        fun createRoute(scheduleId: String? = null): String {
            return if (scheduleId != null) "schedule_edit?scheduleId=$scheduleId" else "schedule_edit"
        }
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 300 }) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -300 }) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Schedules.route) {
            SchedulesScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditSchedule = { scheduleId ->
                    navController.navigate(Screen.ScheduleEdit.createRoute(scheduleId))
                }
            )
        }

        composable(
            route = Screen.ScheduleEdit.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            ScheduleEditScreen(
                viewModel = viewModel,
                scheduleId = scheduleId,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
