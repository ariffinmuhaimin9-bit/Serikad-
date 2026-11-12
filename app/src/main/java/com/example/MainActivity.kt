package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.repository.CardRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GuestManagementScreen
import com.example.ui.screens.RecipientCardScreen
import com.example.ui.screens.StudioEditorScreen
import com.example.ui.screens.TemplatesGalleryScreen
import com.example.ui.theme.SeriKadTheme
import com.example.viewmodel.SeriKadViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_TEMPLATES = "templates"
const val ROUTE_STUDIO = "studio"
const val ROUTE_GUESTS = "guests"

data class NavTabItem(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

class MainActivity : ComponentActivity() {
  private var pendingDeepLinkCode by mutableStateOf<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    extractDeepLink(intent)

    setContent {
      SeriKadTheme {
        AppRootNavigation(
          pendingDeepLinkCode = pendingDeepLinkCode,
          onDeepLinkConsumed = { pendingDeepLinkCode = null },
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    extractDeepLink(intent)
  }

  private fun extractDeepLink(intent: Intent?) {
    val data = intent?.data ?: return
    val segment = data.lastPathSegment?.trim()
    if (!segment.isNullOrBlank()) {
      pendingDeepLinkCode = segment
    }
  }
}

@Composable
fun AppRootNavigation(
  pendingDeepLinkCode: String?,
  onDeepLinkConsumed: () -> Unit,
) {
  val auth = Firebase.auth
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) {
      MainFeatureShell(
        currentUserId = user.uid,
        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Pengirim",
        pendingDeepLinkCode = pendingDeepLinkCode,
        onDeepLinkConsumed = onDeepLinkConsumed,
      )
    }
  }
}

@Composable
fun MainFeatureShell(
  currentUserId: String,
  displayName: String,
  pendingDeepLinkCode: String?,
  onDeepLinkConsumed: () -> Unit,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val viewModel: SeriKadViewModel =
    viewModel(
      key = currentUserId,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            SeriKadViewModel(
              repository = CardRepository(db, Firebase.auth),
              currentUserId = currentUserId,
              initialDisplayName = displayName,
            )
          }
        },
    )

  val cardsState by viewModel.cardsState.collectAsStateWithLifecycle()
  val guestsByCard by viewModel.guestsByCard.collectAsStateWithLifecycle()
  val rsvpsByShareCode by viewModel.rsvpsByShareCode.collectAsStateWithLifecycle()
  val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
  val editorCard by viewModel.editorCard.collectAsStateWithLifecycle()
  val selectedElementId by viewModel.selectedElementId.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSavingCard.collectAsStateWithLifecycle()
  val selectedManageCardId by viewModel.selectedManageCardId.collectAsStateWithLifecycle()
  val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
  val playingTrack by viewModel.playingTrack.collectAsStateWithLifecycle()
  val recipientLookup by viewModel.recipientLookup.collectAsStateWithLifecycle()
  val isSubmittingRsvp by viewModel.isSubmittingRsvp.collectAsStateWithLifecycle()
  val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(snackbarMsg) {
    val msg = snackbarMsg
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  LaunchedEffect(pendingDeepLinkCode) {
    if (!pendingDeepLinkCode.isNullOrBlank()) {
      viewModel.openSharedCardByCode(pendingDeepLinkCode)
      onDeepLinkConsumed()
    }
  }

  val activeRecipientState = recipientLookup
  if (activeRecipientState != null) {
    RecipientCardScreen(
      lookupState = activeRecipientState,
      isMusicPlaying = isMusicPlaying,
      playingTrack = playingTrack,
      isSubmitting = isSubmittingRsvp,
      onToggleMusic = viewModel::toggleMusic,
      onSubmitFeedback = viewModel::submitRecipientFeedback,
      onClose = viewModel::closeRecipientView,
    )
    return
  }

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val navItems = remember {
    listOf(
      NavTabItem(ROUTE_DASHBOARD, R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
      NavTabItem(ROUTE_TEMPLATES, R.string.nav_templates, Icons.Filled.Style, Icons.Outlined.Style),
      NavTabItem(ROUTE_STUDIO, R.string.nav_studio, Icons.Filled.Brush, Icons.Outlined.Brush),
      NavTabItem(ROUTE_GUESTS, R.string.nav_guests, Icons.Filled.Groups, Icons.Outlined.Groups),
    )
  }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      bottomBar = {
        if (!useNavRail) {
          NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = stringResource(id = item.labelRes),
                  )
                },
                label = { Text(stringResource(id = item.labelRes), maxLines = 1) },
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useNavRail) {
          NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = stringResource(id = item.labelRes),
                  )
                },
                label = { Text(stringResource(id = item.labelRes)) },
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
          ) {
            composable(ROUTE_DASHBOARD) {
              DashboardScreen(
                userName = displayName,
                summary = summary,
                cardsState = cardsState,
                onCreateNewCard = { navigateToTab(ROUTE_TEMPLATES) },
                onOpenCode = viewModel::openSharedCardByCode,
                onSelectCardToManage = { card ->
                  viewModel.selectManageCard(card.id)
                  navigateToTab(ROUTE_GUESTS)
                },
                onPreviewCard = viewModel::previewLocalCardAsRecipient,
                onSignOut = {
                  viewModel.stopMusic()
                  signOutUser(credentialManager, coroutineScope)
                },
              )
            }
            composable(ROUTE_TEMPLATES) {
              TemplatesGalleryScreen(
                cardsState = cardsState,
                onUseTemplate = { preset ->
                  viewModel.loadTemplateIntoEditor(preset)
                  navigateToTab(ROUTE_STUDIO)
                },
                onStartBlank = { cat ->
                  viewModel.startBlankCard(cat)
                  navigateToTab(ROUTE_STUDIO)
                },
                onEditCard = { card ->
                  viewModel.editExistingCard(card)
                  navigateToTab(ROUTE_STUDIO)
                },
                onPreviewCard = viewModel::previewLocalCardAsRecipient,
                onDeleteCard = viewModel::deleteCard,
              )
            }
            composable(ROUTE_STUDIO) {
              StudioEditorScreen(
                card = editorCard,
                selectedElementId = selectedElementId,
                isSaving = isSaving,
                isMusicPlaying = isMusicPlaying,
                playingTrack = playingTrack,
                onUpdateCard = viewModel::updateEditorCard,
                onSelectElement = viewModel::selectCanvasElement,
                onMoveElement = viewModel::moveElement,
                onAddText = viewModel::addTextElement,
                onAddSticker = viewModel::addStickerElement,
                onAddPhoto = viewModel::addPhotoElement,
                onUpdateSelectedElement = viewModel::updateSelectedElement,
                onDeleteSelectedElement = viewModel::deleteSelectedElement,
                onToggleMusic = viewModel::toggleMusic,
                onSaveCard = viewModel::saveCurrentCard,
                onPreviewRecipient = viewModel::previewLocalCardAsRecipient,
                onShowMessage = viewModel::showSnackbar,
              )
            }
            composable(ROUTE_GUESTS) {
              GuestManagementScreen(
                cardsState = cardsState,
                selectedCardId = selectedManageCardId,
                guestsByCard = guestsByCard,
                rsvpsByShareCode = rsvpsByShareCode,
                onSelectCard = viewModel::selectManageCard,
                onAddGuest = viewModel::addGuest,
                onMarkSent = viewModel::markGuestSent,
                onUpdateStatus = viewModel::updateGuestStatus,
                onDeleteGuest = viewModel::deleteGuest,
                onShowMessage = viewModel::showSnackbar,
                onGoToTemplates = { navigateToTab(ROUTE_TEMPLATES) },
              )
            }
          }
        }
      }
    }
  }
}
