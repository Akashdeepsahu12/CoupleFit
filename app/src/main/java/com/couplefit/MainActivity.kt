package com.couplefit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.couplefit.ui.components.AmbientMeshGradientBackground
import com.couplefit.ui.components.MeshGradientPreset
import com.couplefit.ui.components.AmbientVideoBackground
import com.couplefit.ui.components.AmbientVideoRegistry
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.data.model.AuthFlowStep
import com.couplefit.ui.auth.AuthViewModel
import com.couplefit.ui.auth.EnterCodeScreen
import com.couplefit.ui.auth.InvitePartnerScreen
import com.couplefit.ui.auth.LinkedConfirmationScreen
import com.couplefit.ui.auth.SignInScreen
import com.couplefit.ui.challenge.ChallengesScreen
import com.couplefit.ui.components.NoInternetDialog
import com.couplefit.ui.dashboard.DashboardAdaptiveScreen
import com.couplefit.ui.hub.CoupleHubScreen
import com.couplefit.ui.navigation.CoupleFitNavigationSuite
import com.couplefit.ui.navigation.TopLevelDestination
import com.couplefit.ui.network.NetworkViewModel
import com.couplefit.ui.settings.SettingsScreen
import com.couplefit.ui.theme.CoupleFitTheme
import com.couplefit.ui.theme.ThemeCircularRevealContainer
import com.couplefit.ui.theme.rememberThemeRevealState
import com.couplefit.ui.trends.HealthTrendsScreen
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingInviteCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        handleDeepLink(intent)

        val prefs = getSharedPreferences("couplefit_prefs", Context.MODE_PRIVATE)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val systemDark = isSystemInDarkTheme()

            val systemAnimationScale = try {
                android.provider.Settings.Global.getFloat(
                    contentResolver,
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f
                )
            } catch (e: Exception) {
                1f
            }
            val isSystemAnimationDisabled = systemAnimationScale == 0f

            var isDarkTheme by remember {
                mutableStateOf(prefs.getBoolean("is_dark_theme", systemDark))
            }
            var userReduceMotion by remember {
                mutableStateOf(prefs.getBoolean("reduce_motion", false))
            }
            var liquidGlassEnabled by remember {
                mutableStateOf(prefs.getBoolean("liquid_glass_enabled", true))
            }
            var meshGradientEnabled by remember {
                mutableStateOf(prefs.getBoolean("expo_mesh_gradient_enabled", false))
            }
            val effectiveReduceMotion = isSystemAnimationDisabled || userReduceMotion

            // Ambient video background shuffle across app opens (BGV-04)
            val currentAmbientBgId = rememberSaveable {
                val lastBgId = prefs.getString("last_ambient_bg_id", null)
                val selected = AmbientVideoRegistry.selectShuffledBackground(lastSelectedId = lastBgId)
                prefs.edit().putString("last_ambient_bg_id", selected.id).apply()
                selected.id
            }
            val currentAmbientBgAsset = remember(currentAmbientBgId) {
                AmbientVideoRegistry.getById(currentAmbientBgId)
            }

            var isSettingsOpen by remember { mutableStateOf(false) }
            var currentTab by remember { mutableStateOf(TopLevelDestination.TODAY) }

            val networkViewModel: NetworkViewModel = hiltViewModel()
            val showOfflineDialog by networkViewModel.showOfflineDialog.collectAsState()

            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            // Process App Link code if present
            LaunchedEffect(pendingInviteCode) {
                pendingInviteCode?.let { code ->
                    authViewModel.onDeepLinkInviteCodeReceived(code)
                    pendingInviteCode = null
                }
            }

            if (isSettingsOpen) {
                BackHandler {
                    isSettingsOpen = false
                }
            }

            val revealState = rememberThemeRevealState()
            val view = androidx.compose.ui.platform.LocalView.current

            fun triggerThemeSwitchWithReveal(newDark: Boolean, origin: androidx.compose.ui.geometry.Offset?) {
                try {
                    val w = view.width
                    val h = view.height
                    if (w > 0 && h > 0) {
                        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        view.draw(canvas)
                        revealState.previousBitmap = bitmap
                    }
                } catch (e: Exception) {
                    revealState.previousBitmap = null
                }
                revealState.revealOrigin = origin ?: androidx.compose.ui.geometry.Offset.Zero
                isDarkTheme = newDark
                prefs.edit().putBoolean("is_dark_theme", newDark).apply()
                revealState.isRevealing = true
            }

            CoupleFitTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThemeCircularRevealContainer(
                        revealState = revealState,
                        reduceMotion = effectiveReduceMotion
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (meshGradientEnabled) {
                                // Persistent Global Living Expo Mesh Gradient Background across ALL screens (MSH-01)
                                AmbientMeshGradientBackground(
                                    modifier = Modifier.fillMaxSize(),
                                    preset = MeshGradientPreset.CoupleFitMidnight,
                                    reduceMotion = effectiveReduceMotion
                                )
                            } else {
                                // Persistent Global Living Ambient Video Background across ALL screens (BGV-06)
                                AmbientVideoBackground(
                                    modifier = Modifier.fillMaxSize(),
                                    videoResId = currentAmbientBgAsset.resId,
                                    reduceMotion = effectiveReduceMotion
                                )
                            }

                            // Main Routing Decision: Auth & Couple Linking Flow vs Dashboard
                            when (val step = authUiState.currentStep) {
                            is AuthFlowStep.SignIn -> {
                                SignInScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToEnterCode = {
                                        authViewModel.navigateToStep(AuthFlowStep.EnterCode)
                                    }
                                )
                            }
                            is AuthFlowStep.InvitePartner -> {
                                InvitePartnerScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToEnterCode = {
                                        authViewModel.navigateToStep(AuthFlowStep.EnterCode)
                                    },
                                    onNavigateBack = {
                                        authViewModel.navigateToStep(AuthFlowStep.SignIn)
                                    }
                                )
                            }
                            is AuthFlowStep.EnterCode -> {
                                EnterCodeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToInvite = {
                                        authViewModel.navigateToStep(AuthFlowStep.InvitePartner)
                                    },
                                    onNavigateBack = {
                                        authViewModel.navigateToStep(AuthFlowStep.SignIn)
                                    }
                                )
                            }
                            is AuthFlowStep.LinkedConfirmation -> {
                                LinkedConfirmationScreen(
                                    partnerId = step.partnerId,
                                    onContinueToDashboard = {
                                        authViewModel.navigateToStep(AuthFlowStep.Dashboard)
                                    }
                                )
                            }
                            is AuthFlowStep.Dashboard -> {
                                // Return-to-Home BackHandler (BCK-01)
                                BackHandler(enabled = !isSettingsOpen && currentTab != TopLevelDestination.TODAY) {
                                    currentTab = TopLevelDestination.TODAY
                                }

                                AnimatedContent(
                                    targetState = isSettingsOpen,
                                    transitionSpec = {
                                        if (effectiveReduceMotion) {
                                            EnterTransition.None.togetherWith(ExitTransition.None)
                                        } else if (targetState) {
                                            (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn())
                                                .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut())
                                        } else {
                                            (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn())
                                                .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut())
                                        }
                                    },
                                    label = "screenTransition"
                                ) { settingsOpen ->
                                    if (settingsOpen) {
                                        SettingsScreen(
                                            onNavigateBack = { isSettingsOpen = false },
                                            isDarkTheme = isDarkTheme,
                                            onToggleDarkTheme = { newDark ->
                                                triggerThemeSwitchWithReveal(newDark, null)
                                            },
                                            onToggleDarkThemeWithOrigin = { newDark, origin ->
                                                triggerThemeSwitchWithReveal(newDark, origin)
                                            },
                                            reduceMotion = effectiveReduceMotion,
                                            onToggleReduceMotion = { newReduce ->
                                                userReduceMotion = newReduce
                                                prefs.edit().putBoolean("reduce_motion", newReduce).apply()
                                            },
                                            liquidGlassEnabled = liquidGlassEnabled,
                                            onToggleLiquidGlass = { newLiquidGlass ->
                                                liquidGlassEnabled = newLiquidGlass
                                                prefs.edit().putBoolean("liquid_glass_enabled", newLiquidGlass).apply()
                                            },
                                            meshGradientEnabled = meshGradientEnabled,
                                            onToggleMeshGradient = { newMesh ->
                                                meshGradientEnabled = newMesh
                                                prefs.edit().putBoolean("expo_mesh_gradient_enabled", newMesh).putBoolean("mesh_gradient_enabled", newMesh).apply()
                                            }
                                        )
                                    } else {
                                        CoupleFitNavigationSuite(
                                            windowSizeClass = windowSizeClass,
                                            selectedDestination = currentTab,
                                            onDestinationSelected = { currentTab = it },
                                            reduceMotion = effectiveReduceMotion,
                                            ambientVideoResId = currentAmbientBgAsset.resId,
                                            liquidGlassEnabled = liquidGlassEnabled
                                        ) { _ ->
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AnimatedContent(
                                                    targetState = currentTab,
                                                    transitionSpec = {
                                                        if (effectiveReduceMotion) {
                                                            EnterTransition.None.togetherWith(ExitTransition.None)
                                                        } else {
                                                            fadeIn(animationSpec = tween(200))
                                                                .togetherWith(fadeOut(animationSpec = tween(150)))
                                                        }
                                                    },
                                                    label = "tabTransition"
                                                ) { activeTab ->
                                                    when (activeTab) {
                                                        TopLevelDestination.TODAY -> {
                                                            DashboardAdaptiveScreen(
                                                                windowSizeClass = windowSizeClass,
                                                                isDarkTheme = isDarkTheme,
                                                                onToggleDarkTheme = {
                                                                    triggerThemeSwitchWithReveal(!isDarkTheme, null)
                                                                },
                                                                reduceMotion = effectiveReduceMotion,
                                                                onOpenSettings = { isSettingsOpen = true },
                                                                onViewAllChallenges = { currentTab = TopLevelDestination.CHALLENGES }
                                                            )
                                                        }
                                                        TopLevelDestination.CHALLENGES -> {
                                                            ChallengesScreen()
                                                        }
                                                        TopLevelDestination.HEALTH_TRENDS -> {
                                                            HealthTrendsScreen()
                                                        }
                                                        TopLevelDestination.COUPLE_HUB -> {
                                                            CoupleHubScreen(
                                                                onNavigateToPrivacySettings = { isSettingsOpen = true }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }

                    NoInternetDialog(
                        isVisible = showOfflineDialog,
                        onDismiss = { networkViewModel.dismissOfflineDialog() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        timber.log.Timber.d("Incoming deep link data: $data")

        val codeFromQuery = data.getQueryParameter("code")
        val codeFromPath = data.lastPathSegment
        val candidate = codeFromQuery ?: codeFromPath
        val sixDigitRegex = Regex("""\b\d{6}\b""")

        val extractedCode = if (!candidate.isNullOrBlank() && candidate.all { it.isDigit() } && candidate.length == 6) {
            candidate
        } else {
            sixDigitRegex.find(data.toString())?.value
        }

        if (!extractedCode.isNullOrBlank()) {
            timber.log.Timber.i("Successfully extracted pairing code: $extractedCode")
            pendingInviteCode = extractedCode
        }
    }
}
