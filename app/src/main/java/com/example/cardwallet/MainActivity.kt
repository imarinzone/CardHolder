package com.example.cardwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.cardwallet.data.model.CardEntity
import com.example.cardwallet.ui.CardViewModel
import com.example.cardwallet.ui.screens.AddEditCardScreen
import com.example.cardwallet.ui.screens.ArchiveScreen
import com.example.cardwallet.ui.screens.CardDetailScreen
import com.example.cardwallet.ui.screens.CategoriesScreen
import com.example.cardwallet.ui.screens.HomeScreen
import com.example.cardwallet.ui.screens.SettingsScreen
import com.example.cardwallet.ui.theme.CardWalletTheme

sealed class Screen {
    data object Home : Screen()
    data object Categories : Screen()
    data object Archive : Screen()
    data object Settings : Screen()
    data class CardDetail(val cardId: Long) : Screen()
    data class AddEditCard(val cardId: Long? = null) : Screen()
}

enum class NavigationTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    CARDS("Cards", Icons.Default.CreditCard, Icons.Outlined.CreditCard),
    CATEGORIES("Categories", Icons.Default.Folder, Icons.Outlined.Folder),
    ARCHIVE("Archive", Icons.Default.Archive, Icons.Outlined.Archive),
    SETTINGS("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CardWalletTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: CardViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedTab by remember { mutableStateOf(NavigationTab.CARDS) }

    val allCards by viewModel.allActiveCards.collectAsState()

    val showBottomBar = currentScreen is Screen.Home ||
        currentScreen is Screen.Categories ||
        currentScreen is Screen.Archive ||
        currentScreen is Screen.Settings

    if (currentScreen !is Screen.Home) {
        BackHandler {
            if (currentScreen is Screen.AddEditCard) {
                val addEdit = currentScreen as Screen.AddEditCard
                currentScreen = if (addEdit.cardId != null) Screen.CardDetail(addEdit.cardId) else Screen.Home
            } else {
                selectedTab = NavigationTab.CARDS
                currentScreen = Screen.Home
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.testTag("main_navigation_bar")) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                currentScreen = when (tab) {
                                    NavigationTab.CARDS -> Screen.Home
                                    NavigationTab.CATEGORIES -> Screen.Categories
                                    NavigationTab.ARCHIVE -> Screen.Archive
                                    NavigationTab.SETTINGS -> Screen.Settings
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onCardClick = { card -> currentScreen = Screen.CardDetail(card.id) },
                            onAddCardClick = { currentScreen = Screen.AddEditCard(null) }
                        )
                    }

                    is Screen.Categories -> {
                        CategoriesScreen(
                            viewModel = viewModel,
                            onCategoryClick = { categoryName ->
                                viewModel.setSelectedCategory(categoryName)
                                selectedTab = NavigationTab.CARDS
                                currentScreen = Screen.Home
                            }
                        )
                    }

                    is Screen.Archive -> {
                        ArchiveScreen(viewModel = viewModel)
                    }

                    is Screen.Settings -> {
                        SettingsScreen(viewModel = viewModel)
                    }

                    is Screen.CardDetail -> {
                        val card = allCards.firstOrNull { it.id == screen.cardId }
                        if (card != null) {
                            CardDetailScreen(
                                card = card,
                                onBack = { currentScreen = Screen.Home },
                                onEdit = { currentScreen = Screen.AddEditCard(card.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(card) },
                                onArchive = {
                                    viewModel.archiveCard(card.id)
                                    currentScreen = Screen.Home
                                }
                            )
                        } else {
                            // Card was deleted or not found
                            HomeScreen(
                                viewModel = viewModel,
                                onCardClick = { c -> currentScreen = Screen.CardDetail(c.id) },
                                onAddCardClick = { currentScreen = Screen.AddEditCard(null) }
                            )
                        }
                    }

                    is Screen.AddEditCard -> {
                        val cardToEdit = screen.cardId?.let { id -> allCards.firstOrNull { it.id == id } }
                        AddEditCardScreen(
                            viewModel = viewModel,
                            cardToEdit = cardToEdit,
                            onBack = {
                                currentScreen = if (screen.cardId != null) Screen.CardDetail(screen.cardId) else Screen.Home
                            },
                            onSaved = {
                                currentScreen = if (screen.cardId != null) Screen.CardDetail(screen.cardId) else Screen.Home
                            }
                        )
                    }
                }
            }
        }
    }
}
