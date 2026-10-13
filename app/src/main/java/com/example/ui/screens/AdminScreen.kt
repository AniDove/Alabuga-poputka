package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.model.*
import com.example.ui.components.RideStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
  viewModel: AppViewModel
) {
  val users by viewModel.users.collectAsState()
  val rides by viewModel.rides.collectAsState()
  val bookings by viewModel.bookings.collectAsState()
  val complaints by viewModel.complaints.collectAsState()
  val locations by viewModel.locations.collectAsState()
  val transactions by viewModel.transactions.collectAsState()

  var currentAdminTab by remember { mutableStateOf("Сводка") }
  val adminTabs = listOf("Сводка", "Поездки", "Пользователи", "Жалобы", "Справочник")

  // Modals
  var showAddLocationDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Панель администратора",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Мониторинг сервиса «Попутка Алабуга»",
              fontSize = 13.sp,
              color = TextSecondary
            )
          }

          Surface(
            color = Color(0xFF1E3A8A),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "ADMIN DEMO",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
          Text(
            text = "Все административные действия и модерация сохраняются в локальном прототипе устройства.",
            color = TextTertiary,
            fontSize = 10.sp,
            modifier = Modifier.padding(8.dp)
          )
        }
      }
    }

    // Tabs row
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(adminTabs) { tab ->
          val isSelected = currentAdminTab == tab
          Surface(
            color = if (isSelected) BluePrimary else SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.clickable { currentAdminTab = tab }
          ) {
            Text(
              text = tab,
              color = if (isSelected) Color.White else TextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    when (currentAdminTab) {
      "Сводка" -> {
        item {
          AdminSummarySection(
            usersCount = users.size,
            totalRides = rides.size,
            activeRides = rides.count { it.status == RideStatus.ACTIVE },
            completedRides = rides.count { it.status == RideStatus.COMPLETED },
            cancelledRides = rides.count { it.status == RideStatus.CANCELLED },
            pendingComplaints = complaints.count { it.status == ComplaintStatus.PENDING },
            totalCommission = transactions.filter { it.type == TransactionType.COMMISSION_FEE }.sumOf { kotlin.math.abs(it.amount) },
            transactions = transactions
          )
        }
      }

      "Поездки" -> {
        item {
          Text("Управление поездками (${rides.size}):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        items(rides, key = { it.id }) { ride ->
          AdminRideItem(
            ride = ride,
            onCancel = { viewModel.adminCancelRide(ride.id) }
          )
        }
      }

      "Пользователи" -> {
        item {
          Text("Управление пользователями (${users.size}):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        items(users, key = { it.id }) { user ->
          AdminUserItem(
            user = user,
            onToggleBlock = { viewModel.adminToggleBlockUser(user.id) }
          )
        }
      }

      "Жалобы" -> {
        item {
          Text("Жалобы и споры (${complaints.size}):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (complaints.isEmpty()) {
          item {
            Surface(
              color = SurfaceDark,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Жалоб нет", color = TextSecondary, modifier = Modifier.padding(16.dp))
            }
          }
        } else {
          items(complaints, key = { it.id }) { cmp ->
            AdminComplaintItem(
              complaint = cmp,
              onResolve = { viewModel.adminResolveComplaint(cmp.id, ComplaintStatus.RESOLVED) },
              onReject = { viewModel.adminResolveComplaint(cmp.id, ComplaintStatus.REJECTED) }
            )
          }
        }
      }

      "Справочник" -> {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Точки маршрута (${locations.size}):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
              onClick = { showAddLocationDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Добавить точку", fontSize = 11.sp)
            }
          }
        }
        items(locations, key = { it.id }) { loc ->
          AdminLocationItem(loc)
        }
      }
    }
  }

  // Add Location Dialog
  if (showAddLocationDialog) {
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("ОЭЗ Алабуга") }
    var newDesc by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddLocationDialog = false },
      containerColor = SurfaceDark,
      title = { Text("Новая точка маршрута", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Название (например, КПП-2 ОЭЗ)") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = BluePrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Елабуга", "ОЭЗ Алабуга").forEach { cat ->
              FilterChip(
                selected = newCategory == cat,
                onClick = { newCategory = cat },
                label = { Text(cat, fontSize = 11.sp) }
              )
            }
          }

          OutlinedTextField(
            value = newDesc,
            onValueChange = { newDesc = it },
            label = { Text("Ориентир или описание") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = BluePrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank()) {
              viewModel.adminAddLocation(newName, newCategory, newDesc)
              showAddLocationDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
          enabled = newName.isNotBlank()
        ) {
          Text("Сохранить")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddLocationDialog = false }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun AdminSummarySection(
  usersCount: Int,
  totalRides: Int,
  activeRides: Int,
  completedRides: Int,
  cancelledRides: Int,
  pendingComplaints: Int,
  totalCommission: Int,
  transactions: List<WalletTransaction>
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // 2x2 Grid of KPIs
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "Всего поездок",
        value = "$totalRides",
        sub = "Активных: $activeRides",
        color = BlueSecondary,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Суммарная комиссия",
        value = "$totalCommission ₽",
        sub = "Тариф 10%",
        color = SuccessGreen,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "Пользователи",
        value = "$usersCount",
        sub = "Водители и пассажиры",
        color = InfoCyan,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Жалобы в очереди",
        value = "$pendingComplaints",
        sub = "Требуют решения",
        color = if (pendingComplaints > 0) WarningAmber else TextSecondary,
        modifier = Modifier.weight(1f)
      )
    }

    // Rides status breakdown
    Surface(
      color = SurfaceDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Статусы поездок сервиса:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Активные: $activeRides", color = SuccessGreen, fontSize = 11.sp)
          Text("Завершенные: $completedRides", color = TextSecondary, fontSize = 11.sp)
          Text("Отмененные: $cancelledRides", color = ErrorRed, fontSize = 11.sp)
        }
      }
    }

    // Financial operations log
    Spacer(modifier = Modifier.height(6.dp))
    Text("Журнал транзакций системы:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    transactions.take(5).forEach { tx ->
      Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(tx.type.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(tx.description, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
          }
          Text("${tx.amount} ₽", color = if (tx.amount > 0) SuccessGreen else WarningAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  sub: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(title, color = TextSecondary, fontSize = 11.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text(sub, color = TextTertiary, fontSize = 10.sp)
    }
  }
}

@Composable
private fun AdminRideItem(
  ride: Ride,
  onCancel: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("${ride.fromLocation} → ${ride.toLocation}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        RideStatusBadge(ride.status)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Водитель: ${ride.driverName} • ${ride.time} • ${ride.pricePerSeat} ₽", color = TextSecondary, fontSize = 11.sp)
      if (ride.status == RideStatus.ACTIVE) {
        Spacer(modifier = Modifier.height(6.dp))
        Button(
          onClick = onCancel,
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.height(28.dp)
        ) {
          Text("Отменить поездку (админ)", fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
private fun AdminUserItem(
  user: User,
  onToggleBlock: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (user.isBlocked) ErrorRed.copy(alpha = 0.5f) else SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = Color(0xFF1E3A8A),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(user.role.title, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
          }
        }
        Text("Тел.: ${user.phone} • Поездок: ${user.tripsCount}" + if (user.role == UserRole.DRIVER) " • Баланс: ${user.balance} ₽" else "", color = TextSecondary, fontSize = 11.sp)
        if (user.isBlocked) {
          Text("СТАТУС: ЗАБЛОКИРОВАН", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }

      OutlinedButton(
        onClick = onToggleBlock,
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (user.isBlocked) SuccessGreen else ErrorRed
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(30.dp)
      ) {
        Text(if (user.isBlocked) "Разблокировать" else "Блокировать", fontSize = 10.sp)
      }
    }
  }
}

@Composable
private fun AdminComplaintItem(
  complaint: Complaint,
  onResolve: () -> Unit,
  onReject: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Жалоба от: ${complaint.authorName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Surface(
          color = when (complaint.status) {
            ComplaintStatus.PENDING -> WarningBg
            ComplaintStatus.RESOLVED -> SuccessBg
            ComplaintStatus.REJECTED -> Color(0xFF262626)
          },
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            complaint.status.title,
            color = when (complaint.status) {
              ComplaintStatus.PENDING -> WarningAmber
              ComplaintStatus.RESOLVED -> SuccessGreen
              ComplaintStatus.REJECTED -> TextSecondary
            },
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text("Объект: ${complaint.targetTitle}", color = BlueSecondary, fontSize = 11.sp)
      Text("Причина: ${complaint.reason}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
      Text("«${complaint.description}»", color = TextSecondary, fontSize = 11.sp)

      if (complaint.status == ComplaintStatus.PENDING) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onResolve,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Удовлетворить", fontSize = 10.sp)
          }

          OutlinedButton(
            onClick = onReject,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Отклонить", fontSize = 10.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun AdminLocationItem(loc: LocationPoint) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(BlueDark),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Place, contentDescription = null, tint = BlueSecondary, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(loc.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("${loc.category} • ${loc.description}", color = TextSecondary, fontSize = 10.sp)
      }
    }
  }
}
