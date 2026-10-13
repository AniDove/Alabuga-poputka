package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TopDemoHeader(
  currentRole: UserRole,
  currentUser: User,
  allUsers: List<User>,
  onRoleSelected: (UserRole) -> Unit,
  onUserSelected: (String) -> Unit
) {
  var showUserDropdown by remember { mutableStateOf(false) }

  Surface(
    color = Color(0xFF0F172A),
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      // Demo Mode Banner
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(WarningAmber)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "ДЕМО-РЕЖИМ • Елабуга & ОЭЗ",
            color = Color(0xFFF1F5F9),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // Active Profile Selector Pill
        Box {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF1E293B))
              .border(1.dp, BluePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
              .clickable { showUserDropdown = true }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser.name.take(1),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentUser.name.split(" ").firstOrNull() ?: "",
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = "Выбор пользователя",
              tint = TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = showUserDropdown,
            onDismissRequest = { showUserDropdown = false },
            modifier = Modifier.background(SurfaceDark)
          ) {
            Text(
              text = "Быстрое переключение демо-аккаунта:",
              color = TextSecondary,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            allUsers.forEach { u ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(
                      text = "${u.name} (${u.role.title})",
                      color = if (u.id == currentUser.id) BlueSecondary else TextPrimary,
                      fontWeight = if (u.id == currentUser.id) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 13.sp
                    )
                    Text(
                      text = if (u.role == UserRole.DRIVER) "${u.carModel} • ${u.balance} ₽" else u.phone,
                      color = TextSecondary,
                      fontSize = 11.sp
                    )
                  }
                },
                onClick = {
                  showUserDropdown = false
                  onUserSelected(u.id)
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3-Way Role Selector Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E293B))
          .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        RoleTabItem(
          title = "Пассажир",
          isSelected = currentRole == UserRole.PASSENGER,
          icon = Icons.Default.Person,
          modifier = Modifier.weight(1f),
          onClick = { onRoleSelected(UserRole.PASSENGER) }
        )
        RoleTabItem(
          title = "Водитель",
          isSelected = currentRole == UserRole.DRIVER,
          icon = Icons.Default.DirectionsCar,
          modifier = Modifier.weight(1f),
          onClick = { onRoleSelected(UserRole.DRIVER) }
        )
        RoleTabItem(
          title = "Админ",
          isSelected = currentRole == UserRole.ADMIN,
          icon = Icons.Default.AdminPanelSettings,
          modifier = Modifier.weight(1f),
          onClick = { onRoleSelected(UserRole.ADMIN) }
        )
      }
    }
  }
}

@Composable
private fun RoleTabItem(
  title: String,
  isSelected: Boolean,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) BluePrimary else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(vertical = 7.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = if (isSelected) Color.White else TextSecondary,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = title,
        color = if (isSelected) Color.White else TextSecondary,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
fun AppBottomNavigation(
  role: UserRole,
  currentScreen: String,
  unreadNotifications: Int,
  unreadChats: Int,
  pendingBookingsCount: Int,
  onNavigate: (String) -> Unit
) {
  NavigationBar(
    containerColor = Color(0xFF0F172A),
    contentColor = TextPrimary,
    tonalElevation = 8.dp,
    modifier = Modifier.navigationBarsPadding()
  ) {
    when (role) {
      UserRole.PASSENGER -> {
        NavigationBarItem(
          selected = currentScreen == "search",
          onClick = { onNavigate("search") },
          icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
          label = { Text("Поиск", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "passenger_rides",
          onClick = { onNavigate("passenger_rides") },
          icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Поездки") },
          label = { Text("Поездки", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "chats" || currentScreen == "chat_thread",
          onClick = { onNavigate("chats") },
          icon = {
            BadgedBox(
              badge = {
                if (unreadChats > 0) {
                  Badge(containerColor = BluePrimary) { Text("$unreadChats") }
                }
              }
            ) {
              Icon(Icons.Default.Chat, contentDescription = "Чаты")
            }
          },
          label = { Text("Чаты", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "notifications",
          onClick = { onNavigate("notifications") },
          icon = {
            BadgedBox(
              badge = {
                if (unreadNotifications > 0) {
                  Badge(containerColor = ErrorRed) { Text("$unreadNotifications") }
                }
              }
            ) {
              Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
            }
          },
          label = { Text("Инфо", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "profile",
          onClick = { onNavigate("profile") },
          icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
          label = { Text("Профиль", fontSize = 11.sp) },
          colors = navBarColors()
        )
      }
      UserRole.DRIVER -> {
        NavigationBarItem(
          selected = currentScreen == "driver_rides",
          onClick = { onNavigate("driver_rides") },
          icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Мои рейсы") },
          label = { Text("Рейсы", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "create_ride",
          onClick = { onNavigate("create_ride") },
          icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Создать") },
          label = { Text("Создать", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "driver_requests",
          onClick = { onNavigate("driver_requests") },
          icon = {
            BadgedBox(
              badge = {
                if (pendingBookingsCount > 0) {
                  Badge(containerColor = WarningAmber) { Text("$pendingBookingsCount") }
                }
              }
            ) {
              Icon(Icons.Default.Assignment, contentDescription = "Заявки")
            }
          },
          label = { Text("Заявки", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "finance",
          onClick = { onNavigate("finance") },
          icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Баланс") },
          label = { Text("Баланс", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "chats" || currentScreen == "chat_thread",
          onClick = { onNavigate("chats") },
          icon = {
            BadgedBox(
              badge = {
                if (unreadChats > 0) {
                  Badge(containerColor = BluePrimary) { Text("$unreadChats") }
                }
              }
            ) {
              Icon(Icons.Default.Chat, contentDescription = "Чаты")
            }
          },
          label = { Text("Чаты", fontSize = 11.sp) },
          colors = navBarColors()
        )
      }
      UserRole.ADMIN -> {
        NavigationBarItem(
          selected = currentScreen == "admin_stats" || currentScreen == "admin",
          onClick = { onNavigate("admin_stats") },
          icon = { Icon(Icons.Default.Dashboard, contentDescription = "Сводка") },
          label = { Text("Сводка", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "admin_rides",
          onClick = { onNavigate("admin_rides") },
          icon = { Icon(Icons.Default.AltRoute, contentDescription = "Поездки") },
          label = { Text("Поездки", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "admin_users",
          onClick = { onNavigate("admin_users") },
          icon = { Icon(Icons.Default.Group, contentDescription = "Люди") },
          label = { Text("Люди", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "admin_complaints",
          onClick = { onNavigate("admin_complaints") },
          icon = { Icon(Icons.Default.ReportProblem, contentDescription = "Жалобы") },
          label = { Text("Жалобы", fontSize = 11.sp) },
          colors = navBarColors()
        )
        NavigationBarItem(
          selected = currentScreen == "admin_locations",
          onClick = { onNavigate("admin_locations") },
          icon = { Icon(Icons.Default.Place, contentDescription = "Точки") },
          label = { Text("Точки", fontSize = 11.sp) },
          colors = navBarColors()
        )
      }
    }
  }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
  selectedIconColor = Color.White,
  selectedTextColor = BlueSecondary,
  unselectedIconColor = TextSecondary,
  unselectedTextColor = TextSecondary,
  indicatorColor = BluePrimary
)

// Schematic Visualizer replacing real maps with elegant route diagram
@Composable
fun SchematicRouteVisualizer(
  from: String,
  to: String,
  time: String = "",
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(
        Brush.horizontalGradient(
          colors = listOf(Color(0xFF1E293B), Color(0xFF233247))
        )
      )
      .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "СХЕМА МАРШРУТА",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = BlueSecondary,
          letterSpacing = 1.sp
        )
        if (time.isNotBlank()) {
          Text(
            text = "Отправление: $time",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Point A
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.widthIn(min = 90.dp)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(SuccessGreen.copy(alpha = 0.2f))
              .border(1.5.dp, SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(SuccessGreen)
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = from,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "Откуда",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }

        // Connecting dynamic route dashed line with car in center
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Divider(
            color = BluePrimary.copy(alpha = 0.6f),
            thickness = 2.dp,
            modifier = Modifier.fillMaxWidth()
          )
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(Color(0xFF1E293B))
              .border(1.dp, BlueSecondary, CircleShape)
              .padding(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = "В пути",
              tint = BlueSecondary,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        // Point B
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.widthIn(min = 90.dp)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(BluePrimary.copy(alpha = 0.2f))
              .border(1.5.dp, BluePrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Place,
              contentDescription = "Куда",
              tint = BluePrimary,
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = to,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "Куда",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

@Composable
fun RideStatusBadge(status: RideStatus) {
  val (bgColor, textColor) = when (status) {
    RideStatus.ACTIVE -> SuccessBg to SuccessGreen
    RideStatus.IN_PROGRESS -> BlueDark to BlueSecondary
    RideStatus.COMPLETED -> Color(0xFF1E293B) to TextSecondary
    RideStatus.UNDER_REVIEW -> WarningBg to WarningAmber
    RideStatus.CANCELLED -> ErrorBg to ErrorRed
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
  ) {
    Text(
      text = status.title,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun BookingStatusBadge(status: BookingStatus) {
  val (bgColor, textColor) = when (status) {
    BookingStatus.PENDING -> WarningBg to WarningAmber
    BookingStatus.CONFIRMED -> SuccessBg to SuccessGreen
    BookingStatus.COMPLETED -> Color(0xFF1E293B) to TextSecondary
    BookingStatus.REJECTED -> ErrorBg to ErrorRed
    BookingStatus.CANCELLED -> Color(0xFF262626) to Color(0xFFA3A3A3)
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
  ) {
    Text(
      text = status.title,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun ComplaintDialog(
  targetType: String,
  targetId: String,
  targetTitle: String,
  onDismiss: () -> Unit,
  onSubmit: (reason: String, description: String) -> Unit
) {
  var reason by remember { mutableStateOf("Неприемлемое поведение") }
  var description by remember { mutableStateOf("") }
  val reasons = listOf(
    "Неприемлемое поведение",
    "Опоздание или срыв поездки",
    "Несоответствие машины / описания",
    "Курение или мусор в салоне",
    "Спор по оплате / комиссии",
    "Другая причина"
  )
  var showDropdown by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceDark,
    title = {
      Text(
        text = "Подать жалобу",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Объект: $targetTitle",
          color = TextSecondary,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Причина:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { showDropdown = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF131B2E),
              contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(reason, modifier = Modifier.weight(1f), maxLines = 1)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
          }
          DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.background(SurfaceDark)
          ) {
            reasons.forEach { r ->
              DropdownMenuItem(
                text = { Text(r, color = TextPrimary) },
                onClick = {
                  reason = r
                  showDropdown = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Подробное описание:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          placeholder = { Text("Опишите, что произошло...", color = TextTertiary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = Color(0xFF131B2E),
            unfocusedContainerColor = Color(0xFF131B2E),
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = SurfaceBorderDark
          ),
          minLines = 3,
          maxLines = 5,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (description.isNotBlank()) {
            onSubmit(reason, description)
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
        enabled = description.isNotBlank()
      ) {
        Text("Отправить жалобу")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена", color = TextSecondary)
      }
    }
  )
}
