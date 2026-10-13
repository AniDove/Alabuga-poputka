package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppViewModel
import com.example.model.BookingStatus
import com.example.model.UserRole
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.TopDemoHeader
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppContent()
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: AppViewModel = viewModel()) {
  val currentRole by viewModel.activeRole.collectAsState()
  val currentScreen by viewModel.currentScreen.collectAsState()
  val currentUser = viewModel.getCurrentUser()
  val allUsers by viewModel.users.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val chats by viewModel.chats.collectAsState()
  val bookings by viewModel.bookings.collectAsState()
  val rides by viewModel.rides.collectAsState()
  val selectedChatId by viewModel.selectedChatId.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()

  // Badge calculations
  val unreadNotifs = notifications.count { it.userId == currentUser.id && !it.isRead }
  val unreadChats = chats.filter { it.driverId == currentUser.id || it.passengerId == currentUser.id }.sumOf { it.unreadCount }
  val driverRideIds = rides.filter { it.driverId == currentUser.id }.map { it.id }.toSet()
  val pendingRequests = bookings.count { it.rideId in driverRideIds && it.status == BookingStatus.PENDING }

  // Auto-dismiss toast
  LaunchedEffect(toastMessage) {
    if (toastMessage != null) {
      delay(3000)
      viewModel.clearToast()
    }
  }

  // Back handling
  BackHandler(enabled = currentScreen != "search" && currentScreen != "driver_rides" && currentScreen != "admin_stats" && currentScreen != "admin") {
    when (currentRole) {
      UserRole.PASSENGER -> viewModel.navigateTo("search")
      UserRole.DRIVER -> viewModel.navigateTo("driver_rides")
      UserRole.ADMIN -> viewModel.navigateTo("admin")
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = BackgroundDark,
      topBar = {
        if (currentScreen != "chat_thread") {
          TopDemoHeader(
            currentRole = currentRole,
            currentUser = currentUser,
            allUsers = allUsers,
            onRoleSelected = { role ->
              viewModel.switchRole(role)
            },
            onUserSelected = { userId ->
              viewModel.switchUser(userId)
            }
          )
        }
      },
      bottomBar = {
        if (currentScreen != "chat_thread") {
          AppBottomNavigation(
            role = currentRole,
            currentScreen = currentScreen,
            unreadNotifications = unreadNotifs,
            unreadChats = unreadChats,
            pendingBookingsCount = pendingRequests,
            onNavigate = { screen ->
              viewModel.navigateTo(screen)
            }
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when {
          currentScreen == "chat_thread" && selectedChatId != null -> {
            ChatConversationScreen(
              viewModel = viewModel,
              chatId = selectedChatId!!,
              onBack = { viewModel.closeChatThread() }
            )
          }

          currentRole == UserRole.PASSENGER -> {
            when (currentScreen) {
              "search" -> {
                PassengerSearchScreen(
                  viewModel = viewModel,
                  onNavigateToChats = { viewModel.navigateTo("chats") }
                )
              }
              "passenger_rides" -> {
                PassengerRidesScreen(
                  viewModel = viewModel,
                  onNavigateToChat = { viewModel.navigateTo("chats") }
                )
              }
              "chats" -> {
                ChatsListScreen(
                  viewModel = viewModel,
                  onOpenThread = { chatId -> viewModel.selectChat(chatId) }
                )
              }
              "notifications" -> {
                NotificationsScreen(
                  viewModel = viewModel,
                  onOpenRide = { viewModel.navigateTo("passenger_rides") }
                )
              }
              "profile" -> {
                ProfileScreen(viewModel = viewModel)
              }
              else -> {
                PassengerSearchScreen(
                  viewModel = viewModel,
                  onNavigateToChats = { viewModel.navigateTo("chats") }
                )
              }
            }
          }

          currentRole == UserRole.DRIVER -> {
            when (currentScreen) {
              "driver_rides" -> {
                DriverRidesScreen(
                  viewModel = viewModel,
                  onCreateRideClick = { viewModel.navigateTo("create_ride") },
                  onNavigateToRequests = { viewModel.navigateTo("driver_requests") },
                  onOpenChat = { rideId ->
                    viewModel.openChatForRide(rideId)
                  }
                )
              }
              "create_ride" -> {
                CreateRideScreen(
                  viewModel = viewModel,
                  onRideCreated = { viewModel.navigateTo("driver_rides") }
                )
              }
              "driver_requests" -> {
                DriverRequestsScreen(
                  viewModel = viewModel,
                  onOpenChat = { rideId -> viewModel.openChatForRide(rideId) }
                )
              }
              "finance" -> {
                FinanceScreen(viewModel = viewModel)
              }
              "chats" -> {
                ChatsListScreen(
                  viewModel = viewModel,
                  onOpenThread = { chatId -> viewModel.selectChat(chatId) }
                )
              }
              "notifications" -> {
                NotificationsScreen(
                  viewModel = viewModel,
                  onOpenRide = { viewModel.navigateTo("driver_rides") }
                )
              }
              "profile" -> {
                ProfileScreen(viewModel = viewModel)
              }
              else -> {
                DriverRidesScreen(
                  viewModel = viewModel,
                  onCreateRideClick = { viewModel.navigateTo("create_ride") },
                  onNavigateToRequests = { viewModel.navigateTo("driver_requests") },
                  onOpenChat = { rideId -> viewModel.openChatForRide(rideId) }
                )
              }
            }
          }

          currentRole == UserRole.ADMIN -> {
            when (currentScreen) {
              "admin_stats", "admin", "admin_rides", "admin_users", "admin_complaints", "admin_locations" -> {
                AdminScreen(viewModel = viewModel)
              }
              "chats" -> {
                ChatsListScreen(
                  viewModel = viewModel,
                  onOpenThread = { chatId -> viewModel.selectChat(chatId) }
                )
              }
              "profile" -> {
                ProfileScreen(viewModel = viewModel)
              }
              else -> {
                AdminScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }

    // Floating Toast / Snackbar banner
    toastMessage?.let { msg ->
      Snackbar(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 75.dp, start = 16.dp, end = 16.dp),
        containerColor = Color(0xFF1E293B),
        contentColor = Color.White,
        action = {
          TextButton(onClick = { viewModel.clearToast() }) {
            Text("OK", color = Color(0xFF60A5FA))
          }
        }
      ) {
        Text(msg)
      }
    }
  }
}
