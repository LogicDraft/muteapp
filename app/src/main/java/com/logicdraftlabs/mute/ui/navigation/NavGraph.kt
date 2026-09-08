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
import com.logicdraftlabs.mute.ui.screens.LookAndFeelScreen
import com.logicdraftlabs.mute.ui.screens.ScheduleEditScreen
import com.logicdraftlabs.mute.ui.screens.SchedulesScreen
import com.logicdraftlabs.mute.ui.screens.SettingsScreen
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object LookAndFeel : Screen("look_and_feel")
    object Schedules : Screen("schedules")
    object ScheduleEdit : Screen("schedule_edit?scheduleId={scheduleId}") {
        fun createRoute(scheduleId: String? = null) = if (scheduleId == null) "schedule_edit" else "schedule_edit?scheduleId=$scheduleId"
    }
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
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToLookAndFeel = { navController.navigate(Screen.LookAndFeel.route) },
                onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) }
            )
        }

        composable(Screen.LookAndFeel.route) {
            LookAndFeelScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Schedules.route) {
            SchedulesScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditSchedule = { scheduleId -> navController.navigate(Screen.ScheduleEdit.createRoute(scheduleId)) }
            )
        }

        composable(
            route = Screen.ScheduleEdit.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            ScheduleEditScreen(
                viewModel = viewModel,
                scheduleId = entry.arguments?.getString("scheduleId"),
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
